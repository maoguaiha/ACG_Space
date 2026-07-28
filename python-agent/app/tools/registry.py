"""Function-calling 工具注册表（OpenAI 兼容 tools schema）。

LLM 通过这里声明的 schema 自主决定何时调用只读 Bangumi 工具。
"""

TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "search_bangumi",
            "description": "在 Bangumi 全局番剧库中按关键词搜索番剧，返回番名、中文名、简介、评分与链接。推荐类问题首选此工具，用于「类似 XX 的番」「有什么机战番」「搜索某番」等推荐/发现类问题。",
            "parameters": {
                "type": "object",
                "properties": {
                    "keyword": {
                        "type": "string",
                        "description": "搜索关键词，可为番剧名、中文名或类型词（如「CLANNAD」「机战」「治愈」）。",
                    },
                    "limit": {
                        "type": "integer",
                        "description": "返回条数上限，默认 5。",
                    },
                },
                "required": ["keyword"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_bangumi_detail",
            "description": "根据 Bangumi 番剧 ID 获取详情：简介、评分、标签、总集数。当用户点开某部番想了解细节时调用。",
            "parameters": {
                "type": "object",
                "properties": {
                    "bgm_id": {
                        "type": "integer",
                        "description": "Bangumi 番剧 ID（来自 search_bangumi 结果的 id 字段）。",
                    }
                },
                "required": ["bgm_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_airing_now",
            "description": "获取本周正在放送的番剧列表（按星期分组），用于「这季新番」「这周有什么番」等问题。",
            "parameters": {"type": "object", "properties": {}},
        },
    },
]
