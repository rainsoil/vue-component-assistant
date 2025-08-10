#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
VueKit 需求文档索引更新脚本
自动扫描需求文档并更新README.md索引
"""

import os
import re
from datetime import datetime
from pathlib import Path

def parse_requirement_file(file_path):
    """解析需求文档，提取基本信息"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 提取文档信息
    info = {
        'filename': file_path.name,
        'title': '',
        'version': '',
        'date': '',
        'status': '',
        'priority': '',
        'type': '',
        'description': ''
    }
    
    # 提取标题
    title_match = re.search(r'^# (.+)', content, re.MULTILINE)
    if title_match:
        info['title'] = title_match.group(1)
    
    # 提取版本
    version_match = re.search(r'\*\*文档版本\*\*:\s*(.+)', content)
    if version_match:
        info['version'] = version_match.group(1)
    
    # 提取日期
    date_match = re.search(r'\*\*创建日期\*\*:\s*(.+)', content)
    if date_match:
        info['date'] = date_match.group(1)
    
    # 提取状态
    status_match = re.search(r'\*\*文档状态\*\*:\s*(.+)', content)
    if status_match:
        info['status'] = status_match.group(1)
    
    # 提取优先级
    priority_match = re.search(r'\*\*优先级\*\*:\s*(.+)', content)
    if priority_match:
        info['priority'] = priority_match.group(1)
    
    # 提取需求类型
    type_match = re.search(r'\*\*需求类型\*\*:\s*(.+)', content)
    if type_match:
        info['type'] = type_match.group(1)
    
    # 提取描述 (需求概述部分的第一段)
    desc_match = re.search(r'## 📋 需求概述\n\n(.+?)(?=\n\n|\n##)', content, re.DOTALL)
    if desc_match:
        info['description'] = desc_match.group(1).strip()
    
    return info

def generate_index():
    """生成需求文档索引"""
    requirements_dir = Path(__file__).parent
    
    # 扫描所有需求文档
    requirement_files = []
    for file_path in requirements_dir.glob('*_requirements_*.md'):
        info = parse_requirement_file(file_path)
        requirement_files.append(info)
    
    # 按日期排序
    requirement_files.sort(key=lambda x: x['date'], reverse=True)
    
    # 生成索引内容
    index_content = f"""# VueKit 需求文档中心

本文件夹包含VueKit项目的所有需求文档，按时间和类型进行分类管理。

*自动生成于: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}*

## 📋 文档索引

"""
    
    # 按类型分组
    feature_reqs = [req for req in requirement_files if 'feature' in req['filename'] or '功能需求' in req['type']]
    optimization_reqs = [req for req in requirement_files if 'optimization' in req['filename'] or '优化需求' in req['type']]
    maintenance_reqs = [req for req in requirement_files if 'maintenance' in req['filename'] or '维护需求' in req['type']]
    architecture_reqs = [req for req in requirement_files if 'architecture' in req['filename'] or '架构需求' in req['type']]
    
    # 生成功能需求部分
    if feature_reqs:
        index_content += "### 🎯 功能需求\\n\\n"
        for req in feature_reqs:
            index_content += f"""#### {req['title']} ({req['date']})
**文档**: [{req['filename']}]({req['filename']})  
**状态**: {req['status']}  
**优先级**: {req['priority']}  

**描述**: {req['description'][:100]}{'...' if len(req['description']) > 100 else ''}

---

"""
    
    # 生成优化需求部分
    if optimization_reqs:
        index_content += "### 🔧 优化需求\\n\\n"
        for req in optimization_reqs:
            index_content += f"""#### {req['title']} ({req['date']})
**文档**: [{req['filename']}]({req['filename']})  
**状态**: {req['status']}  
**优先级**: {req['priority']}  

**描述**: {req['description'][:100]}{'...' if len(req['description']) > 100 else ''}

---

"""
    
    # 添加统计信息
    total_reqs = len(requirement_files)
    pending_reqs = len([req for req in requirement_files if '待实施' in req['status']])
    completed_reqs = len([req for req in requirement_files if '已完成' in req['status']])
    
    index_content += f"""## 📊 需求统计

### 当前需求概览
```
总需求数量: {total_reqs}个
├── 功能需求: {len(feature_reqs)}个
├── 优化需求: {len(optimization_reqs)}个
├── 维护需求: {len(maintenance_reqs)}个
└── 架构需求: {len(architecture_reqs)}个

按状态分布:
├── 待实施: {pending_reqs}个 ({pending_reqs/total_reqs*100:.0f}%)
└── 已完成: {completed_reqs}个 ({completed_reqs/total_reqs*100:.0f}%)
```

## 📅 需求时间线

"""
    
    # 生成时间线
    for req in requirement_files:
        index_content += f"- **{req['date']}**: [{req['title']}]({req['filename']}) ({req['type']})\\n"
    
    # 添加文档管理说明
    index_content += """
## 📝 文档管理

### 命名规范
- 格式: `{类型}_{描述}_{日期}.md`
- 日期: `YYYYMMDD` 格式
- 类型: `feature`, `optimization`, `maintenance`, `architecture`

### 更新索引
运行以下命令更新此索引文件:
```bash
python update_index.py
```

---

**最后更新**: """ + datetime.now().strftime('%Y-%m-%d %H:%M:%S') + """  
**维护者**: VueKit Team
"""
    
    return index_content

def main():
    """主函数"""
    print("🔄 正在更新需求文档索引...")
    
    try:
        # 生成索引内容
        index_content = generate_index()
        
        # 写入README.md
        readme_path = Path(__file__).parent / 'README.md'
        with open(readme_path, 'w', encoding='utf-8') as f:
            f.write(index_content)
        
        print("✅ 需求文档索引更新完成!")
        print(f"📄 索引文件: {readme_path}")
        
    except Exception as e:
        print(f"❌ 更新索引时发生错误: {e}")

if __name__ == "__main__":
    main()