import re,os
import numpy as np
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
from typing import *

class SemanticChunker:
    def __init__(self, model_name='paraphrase-multilingual-MiniLM-L12-v2'):
        """
        初始化语义分块器
        model_name: 轻量级多语言模型，适合中文
        """
        self.model = SentenceTransformer(model_name)
        self.max_chunk_size = 500  # 最大块大小（字符数）
        self.min_chunk_size = 150  # 最小块大小
        self.sim_threshold = 0.6  # 相似度阈值（低于此值切分）

    def _split_sentences(self, text: str) -> List[str]:
        """改进的句子切分，保留标点"""
        # 按中文句号、问号、感叹号、分号、换行切分
        sentences = re.split(r'([。！？；\n])', text)

        # 重组，保留标点
        result = []
        for i in range(0, len(sentences) - 1, 2):
            if sentences[i].strip():
                result.append(sentences[i] + sentences[i + 1])
        # 处理最后可能没有标点的情况
        if len(sentences) % 2 == 1 and sentences[-1].strip():
            result.append(sentences[-1])

        # 过滤空字符串，去除首尾空格
        result = [s.strip() for s in result if s.strip()]
        return result

    def _compute_similarities(self, sentences: List[str]) -> List[float]:
        """计算相邻句子的相似度"""
        if len(sentences) <= 1:
            return []

        # 批量编码（加速）
        embeddings = self.model.encode(sentences, convert_to_numpy=True)

        # 计算相邻相似度
        similarities = []
        for i in range(len(embeddings) - 1):
            sim = cosine_similarity([embeddings[i]], [embeddings[i + 1]])[0][0]
            similarities.append(sim)

        return similarities

    def chunk(self, text: str) -> List[str]:
        """主分块方法"""
        if not text or not text.strip():
            return []

        # 如果文本很短，直接返回
        if len(text) < self.min_chunk_size:
            return [text.strip()]

        # 1. 切分成句子
        sentences = self._split_sentences(text)

        # 如果句子太少，直接返回
        if len(sentences) <= 1:
            return [text.strip()]

        # 2. 计算相似度
        similarities = self._compute_similarities(sentences)

        # 3. 如果相似度列表为空（只有1个句子），直接返回
        if not similarities:
            return [text.strip()]

        # 4. 动态分块
        chunks = []
        current_chunk = sentences[0]

        for i in range(1, len(sentences)):
            # 获取当前句与前一句的相似度
            sim = similarities[i - 1]

            # 判断是否应该切分
            should_split = False

            # 条件1：相似度低于阈值，且当前块有一定长度
            if sim < self.sim_threshold and len(current_chunk) >= self.min_chunk_size:
                should_split = True

            # 条件2：当前块太长（超过max_chunk_size）
            if len(current_chunk) + len(sentences[i]) > self.max_chunk_size:
                should_split = True

            # 条件3：当前块太短且是最后一个句子，强制合并
            if i == len(sentences) - 1 and len(current_chunk) < self.min_chunk_size:
                should_split = False

            if should_split:
                # 保存当前块
                if current_chunk.strip():
                    chunks.append(current_chunk.strip())
                # 开始新块
                current_chunk = sentences[i]
            else:
                # 合并到当前块
                current_chunk += sentences[i]

        # 添加最后一块
        if current_chunk.strip():
            chunks.append(current_chunk.strip())

        # 如果分块结果为空，返回原文本
        if not chunks:
            return [text.strip()]

        return chunks

    def chunk_with_overlap(self, text: str, overlap_ratio: float = 0.1) -> List[str]:
        """带重叠的分块（可选）"""
        chunks = self.chunk(text)

        if len(chunks) <= 1:
            return chunks

        # 添加重叠：每个块包含前一个块的最后部分
        result = []
        for i, chunk in enumerate(chunks):
            if i > 0:
                # 取前一个块末尾10%的内容作为重叠
                prev_chunk = chunks[i - 1]
                overlap_len = min(int(len(prev_chunk) * overlap_ratio), 100)
                if overlap_len > 0:
                    overlap_text = prev_chunk[-overlap_len:]
                    chunk = overlap_text + chunk
            result.append(chunk)

        return result


if __name__ == "__main__":
    text = """智能手机与个人电脑在硬件架构上存在显著差异，但近年来两者技术边界日益模糊。手机SoC（系统级芯片）集成了CPU、GPU、NPU和基带芯片，代表产品包括高通骁龙8 Gen 3、联发科天玑9300和苹果A17 Pro。这些芯片采用台积电3nm或4nm制程工艺，晶体管数量超过200亿个，性能已接近入门级桌面处理器。

    电脑处理器则分为Intel和AMD两大阵营。Intel酷睿Ultra系列采用Intel 4制程，搭载NPU单元以加速AI应用，最高频率可达5.4GHz。AMD锐龙8000系列基于Zen 4架构，拥有多达16核心32线程，三级缓存容量达到64MB。高端桌面处理器功耗可达250瓦以上，需要配备360mm一体式水冷散热器。

    在存储方面，手机采用LPDDR5X内存和UFS 4.0闪存，理论读写速度分别达到8533Mbps和4200MB/s。而电脑则使用DDR5内存，频率可达8000MHz以上，配合PCIe 5.0固态硬盘，连续读取速度可突破14000MB/s。两者差异主要源于功耗限制和物理空间约束，手机存储芯片尺寸仅为电脑M.2 SSD的十分之一。

    屏幕技术同样存在分野。手机屏幕以OLED为主，支持120Hz自适应刷新率和2160Hz高频PWM调光，峰值亮度可达4500尼特。电脑显示器则呈现多元化格局，电竞玩家偏爱240Hz高刷TN面板，设计师选择4K IPS屏覆盖DCI-P3色域，而高端专业显示器采用Mini LED背光技术，实现HDR1400认证的峰值亮度和局部调光控制。

    显卡是电脑区别于手机的关键部件。NVIDIA RTX 4090拥有16384个CUDA核心和24GB GDDR6X显存，功耗高达450瓦，可流畅运行8K分辨率游戏。AMD RX 7900 XTX则配备6144个流处理器，显存带宽达到960GB/s。手机GPU受限于散热和功耗，性能仅相当于入门级桌面显卡的十分之一，但足以满足移动端游戏需求。

    随着AI大模型普及，端侧算力成为共同趋势。手机NPU算力已突破40TOPS，支持本地运行70亿参数大语言模型。电脑则借助RTX显卡的Tensor Core，可提供超过1300TOPS的AI算力，实现实时语音翻译、图像生成和视频超分等复杂任务。未来，ARM架构可能进一步入侵PC领域，而x86芯片也在尝试降低功耗进入移动设备，两者融合将重塑整个计算产业生态。"""
    # 初始化分块器
    chunker = SemanticChunker()

    # 执行分块
    chunks = chunker.chunk(text)

    # 打印结果
    print(f"总共 {len(chunks)} 块")
    for i, chunk in enumerate(chunks):
        print(f"[{i}] len={len(chunk)}: {chunk[:80]}...")
        print("-" * 50)