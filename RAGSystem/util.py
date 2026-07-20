import os,sys,json
from typing import *

emojis = "✅❌"

def load_documents(data_dir: str) -> List[Dict[str, str]]:
    """加载知识库文档"""
    documents = []

    if not os.path.exists(data_dir):
        print(f"⚠️ 目录不存在: {data_dir}，使用默认文档")
        return get_default_documents()

    for filename in os.listdir(data_dir):
        filepath = os.path.join(data_dir, filename)

        try:
            if filename.endswith('.json'):
                with open(filepath, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                for item in data:
                    documents.append({
                        'content': item['content'],
                        'extra_metadata': item.get('metadata', {'source': filename})
                    })
            elif filename.endswith('.txt'):
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                documents.append({
                    'content': content,
                    'extra_metadata': {'source': filename, 'type': 'txt'}
                })
            # 支持更多格式...
            else:
                print(f"⏭️ 跳过未知格式: {filename}")
        except Exception as e:
            print(f"❌ 加载 {filename} 失败: {e}")

    return documents if documents else get_default_documents()

def load_documents_from_file(file_path : str) -> List[Dict[str, str]]:
    """加载知识库文档"""
    documents = []
    try:
        if file_path.endswith('.json'):
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            for item in data:
                documents.append({
                    'content': item['content'],
                    'extra_metadata': item.get('metadata', {'source': file_path})
                })
        else:
            print(f"⏭️ 跳过未知格式: {file_path}")
    except Exception as e:
        print(f"❌ 加载 {file_path} 失败: {e}")

    return documents if documents else get_default_documents()

def get_default_documents() -> List[Dict[str, str]]:
    """默认文档（当知识库目录为空时使用）"""
    return [
        {
            'content': '人工智能...',
            'extra_metadata': {'category': 'AI基础'}
        },
        # ...
    ]