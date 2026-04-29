import pika,time,json,signal,sys
from datetime import datetime

# 全局变量
connection = None
channel = None
should_exit = False

def signal_handler(sig, frame):
    global should_exit
    print(' [*] 正在关闭连接...')
    should_exit = True
    if channel and channel.is_open:
        channel.stop_consuming()
    if connection and connection.is_open:
        connection.close()
    sys.exit(0)


signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)


def simulate_modeling() -> str:
    print("正在进行三维重建...")
    time.sleep(3)
    return "https://pics4.baidu.com/feed/562c11dfa9ec8a13731d05c9ea190781a0ecc0b8.jpeg@f_auto?token=1c030de4e2810dd95cba30caf6bfd542"


def callback(ch, method, properties, body):
    try:
        # 解析JSON消息
        task_data = json.loads(body.decode())
        task_id = task_data.get('captureId')
        mediaFileUrls = task_data.get('mediaFileUrls')

        print(f" [x] 收到任务: taskId={task_id}, mediaFileUrls={mediaFileUrls}")

        # 记录开始时间
        start_time = time.time()

        # 执行计算
        result_value = simulate_modeling()

        # 计算耗时
        elapsed_time = int((time.time() - start_time) * 1000)

        # 构建结果消息
        result_message = {
            'taskId': task_id,
            'resultUrl': result_value,
            'completeTime': datetime.now().isoformat(),
            'elapsedTime': elapsed_time
        }

        # 发送结果（添加重试机制）
        max_retries = 3
        for retry in range(max_retries):
            try:
                ch.basic_publish(
                    exchange='analysis.direct_exchange',
                    routing_key='analysis.result',
                    body=json.dumps(result_message),
                    properties=pika.BasicProperties(
                        delivery_mode=2,  # 消息持久化
                        content_type='application/json'
                    )
                )
                break
            except pika.exceptions.ChannelClosed as e:
                print(f" [!] 发送结果失败，重试 {retry + 1}/{max_retries}: {e}")
                time.sleep(0.5)
        else:
            print(f" [x] 发送结果失败，已达最大重试次数")
            # 不确认消息，让它重新入队
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
            return

        print(f" [y] 三维重建完成，结果已发送 (耗时: {elapsed_time}ms)")

        # 手动确认任务已被处理
        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        print(f" [x] 处理任务时出错: {e}")
        import traceback
        traceback.print_exc()
        # 拒收消息，不重新入队（会进入死信队列）
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

def main():
    global connection, channel

    while not should_exit:
        try:
            # 建立连接
            connection = pika.BlockingConnection(
                pika.ConnectionParameters(
                    host='localhost',
                    port=5672,
                    heartbeat=600,
                    blocked_connection_timeout=300
                )
            )
            channel = connection.channel()

            # 声明死信交换机（匹配后端配置）
            channel.exchange_declare(
                exchange='analysis.dead_letter_exchange',
                durable=True,
                exchange_type='direct'
            )

            # 声明主交换机（匹配后端配置）
            channel.exchange_declare(
                exchange='analysis.direct_exchange',
                durable=True,
                exchange_type='direct'
            )

            # 声明死信队列（匹配后端配置）
            channel.queue_declare(
                queue='analysis.dead_letter_queue',
                durable=True
            )

            # 绑定死信队列到死信交换机（使用后端配置的路由键）
            channel.queue_bind(
                exchange='analysis.dead_letter_exchange',
                queue='analysis.dead_letter_queue',
                routing_key='analysis.dead_letter'
            )

            # 声明任务队列（完全匹配后端配置）
            # 注意：后端配置中没有设置 x-message-ttl 和 x-max-length
            channel.queue_declare(
                queue='analysis.task_queue',
                durable=True,
                arguments={
                    'x-dead-letter-exchange': 'analysis.dead_letter_exchange',
                    'x-dead-letter-routing-key': 'analysis.dead_letter'
                    # 不设置 x-message-ttl 和 x-max-length，与后端完全一致
                }
            )

            # 声明结果队列（匹配后端配置，结果队列没有死信参数）
            channel.queue_declare(
                queue='analysis.result_queue',
                durable=True
                # 结果队列不需要死信配置，与后端一致
            )

            # 绑定任务队列到主交换机
            channel.queue_bind(
                exchange='analysis.direct_exchange',
                queue='analysis.task_queue',
                routing_key='analysis.task'
            )

            # 绑定结果队列到主交换机
            channel.queue_bind(
                exchange='analysis.direct_exchange',
                queue='analysis.result_queue',
                routing_key='analysis.result'
            )

            # 设置每次只取一条消息（匹配后端的 prefetchCount=1）
            channel.basic_qos(prefetch_count=1)

            # 开始消费（手动确认模式）
            channel.basic_consume(
                queue='analysis.task_queue',
                on_message_callback=callback,
                auto_ack=False  # 手动确认，匹配后端的 AcknowledgeMode.MANUAL
            )

            print('=' * 60)
            print(' [*] Python 算法服务启动')
            print(' [*] 等待任务中... (按 Ctrl+C 退出)')
            print('=' * 60)
            print(f' [✓] 任务队列: analysis.task_queue')
            print(f' [✓] 结果队列: analysis.result_queue')
            print(f' [✓] 死信队列: analysis.dead_letter_queue')
            print(f' [✓] 手动确认模式: 已启用')
            print(f' [✓] 预取数量: 1')
            print('=' * 60)

            channel.start_consuming()

        except pika.exceptions.AMQPConnectionError as e:
            print(f" [x] 连接RabbitMQ失败: {e}")
            print(" [*] 5秒后重试...")
            time.sleep(5)
        except pika.exceptions.ChannelClosed as e:
            print(f" [x] 通道关闭: {e}")
            print(" [*] 提示：如果看到参数不匹配错误，请先删除现有队列：")
            print("     sudo rabbitmqctl delete_queue analysis.task_queue")
            print("     sudo rabbitmqctl delete_queue analysis.result_queue")
            print("     sudo rabbitmqctl delete_queue analysis.dead_letter_queue")
            time.sleep(3)
        except Exception as e:
            print(f" [x] 发生错误: {e}")
            import traceback
            traceback.print_exc()
            time.sleep(3)
        finally:
            if connection and connection.is_open:
                try:
                    connection.close()
                except:
                    pass


if __name__ == '__main__':
    main()