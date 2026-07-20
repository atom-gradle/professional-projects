import sys
import time
from enum import Enum
from typing import Optional


class LogLevel(Enum):
    """日志级别枚举"""
    DEBUG = 1
    INFO = 2
    ERROR = 3


class Log:
    """
    日志类（静态方法版本），支持不同级别和颜色的日志输出

    颜色说明:
        - DEBUG: 浅蓝色 (Light Blue)
        - INFO:  中绿色 (Medium Green)
        - ERROR: 红色 (Red)

    使用方法:
        Log.debug("这是一条调试信息")
        Log.info("这是一条信息日志")
        Log.error("这是一条错误日志")

        # 支持格式化
        Log.debug("用户 {} 登录成功", "Alice")
        Log.info("处理了 {count} 条记录", count=42)
    """

    class Colors:
        RESET = '\033[0m'
        LIGHT_BLUE = '\033[38;2;70;130;255m'  # 更深一点的蓝色 (RGB: 70,130,255)
        MEDIUM_GREEN = '\033[38;2;0;180;0m'  # 更深一点的绿色 (RGB: 0,180,0)
        RED = '\033[38;2;255;0;0m'  # 纯红色 (RGB: 255,0,0)
        TIMESTAMP = '\033[90m'  # 灰色时间戳

    # 类级别配置
    _level = LogLevel.DEBUG
    _enable_timestamp = True
    _enable_color = True
    _output = sys.stdout

    # 级别映射
    _level_map = {
        LogLevel.DEBUG: "DEBUG",
        LogLevel.INFO: "INFO",
        LogLevel.ERROR: "ERROR"
    }

    # 级别颜色映射
    _level_color_map = {
        LogLevel.DEBUG: Colors.LIGHT_BLUE,
        LogLevel.INFO: Colors.MEDIUM_GREEN,
        LogLevel.ERROR: Colors.RED
    }

    # 消息颜色映射
    _msg_color_map = {
        LogLevel.DEBUG: Colors.LIGHT_BLUE,
        LogLevel.INFO: Colors.MEDIUM_GREEN,
        LogLevel.ERROR: Colors.RED
    }

    @classmethod
    def _get_timestamp(cls) -> str:
        """获取当前时间戳字符串"""
        return time.strftime("%Y-%m-%d %H:%M:%S")

    @classmethod
    def _colorize(cls, text: str, color_code: str) -> str:
        """给文本添加颜色"""
        if cls._enable_color:
            return f"{color_code}{text}{cls.Colors.RESET}"
        return text

    @classmethod
    def _log(cls, level: LogLevel, message: str, *args, **kwargs):
        """
        内部日志方法

        Args:
            level: 日志级别
            message: 日志消息
            *args: 格式化参数
            **kwargs: 格式化关键字参数
        """
        # 检查日志级别
        if level.value < cls._level.value:
            return

        # 格式化消息
        if args or kwargs:
            try:
                message = message.format(*args, **kwargs)
            except (IndexError, KeyError):
                # 如果格式化失败，使用原始消息
                pass

        # 构建日志行
        parts = []

        # 时间戳
        if cls._enable_timestamp:
            timestamp = cls._get_timestamp()
            colored_timestamp = cls._colorize(timestamp, cls.Colors.TIMESTAMP)
            parts.append(colored_timestamp)

        # 级别标签
        level_name = cls._level_map[level]
        colored_level = cls._colorize(
            f"[{level_name}]",
            cls._level_color_map[level]
        )
        parts.append(colored_level)

        # 消息内容
        colored_message = cls._colorize(message, cls._msg_color_map[level])
        parts.append(colored_message)

        # 组合并输出
        log_line = " ".join(parts)
        print(log_line, file=cls._output)

    @classmethod
    def debug(cls, message: str, *args, **kwargs):
        """
        输出DEBUG级别日志（浅蓝色）

        Args:
            message: 日志消息
            *args: 格式化参数
            **kwargs: 格式化关键字参数

        Examples:
            Log.debug("调试信息")
            Log.debug("用户 {} 登录", "Alice")
        """
        cls._log(LogLevel.DEBUG, message, *args, **kwargs)

    @classmethod
    def info(cls, message: str, *args, **kwargs):
        """
        输出INFO级别日志（中绿色）

        Args:
            message: 日志消息
            *args: 格式化参数
            **kwargs: 格式化关键字参数

        Examples:
            Log.info("系统启动完成")
            Log.info("处理了 {count} 条记录", count=42)
        """
        cls._log(LogLevel.INFO, message, *args, **kwargs)

    @classmethod
    def error(cls, message: str, *args, **kwargs):
        """
        输出ERROR级别日志（红色）

        Args:
            message: 日志消息
            *args: 格式化参数
            **kwargs: 格式化关键字参数

        Examples:
            Log.error("数据库连接失败")
            Log.error("错误码: {code}, 信息: {msg}", code=500, msg="Internal Error")
        """
        cls._log(LogLevel.ERROR, message, *args, **kwargs)

    @classmethod
    def set_level(cls, level: LogLevel):
        """
        设置日志级别

        Args:
            level: 新的日志级别

        Examples:
            Log.set_level(LogLevel.INFO)  # 只显示INFO及以上级别
        """
        cls._level = level

    @classmethod
    def get_level(cls) -> LogLevel:
        """获取当前日志级别"""
        return cls._level

    @classmethod
    def enable_timestamp(cls, enabled: bool = True):
        """
        启用/禁用时间戳

        Args:
            enabled: True启用，False禁用
        """
        cls._enable_timestamp = enabled

    @classmethod
    def enable_color(cls, enabled: bool = True):
        """
        启用/禁用颜色输出

        Args:
            enabled: True启用，False禁用
        """
        cls._enable_color = enabled

    @classmethod
    def set_output(cls, output):
        """
        设置输出流

        Args:
            output: 输出流对象，如 sys.stdout, sys.stderr 或文件对象
        """
        cls._output = output
