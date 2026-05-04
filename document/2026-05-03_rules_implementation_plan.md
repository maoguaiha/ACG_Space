规则执行与自动化实施计划（2026-05-03）

目标：把仓库内的“Global Rule (简易版).md”落地为可执行的检查与团队流程，降低人为疏漏的概率。

已实现项：
- 在 `.agents/rules/Global Rule (简易版).md` 中新增“执行计划与自动化”章节，明确了要实现的检测项和 CI 集成建议。
- 添加本地/CI 校验脚本：`tools/verify_rules.ps1`（PowerShell），包含：
  - 检查 `backend/pom.xml` 是否声明 Fastjson2；
  - 检查 `front-ui` / `admin-ui` 是否存在 `tsconfig.json`；
  - 报告 `front-ui` 下的 `.js` 文件（需迁移）；
  - 简易扫描 Java 控制器中 `@RequestBody` 的 `@Validated` 缺失可疑项（需要人工复核）。
- 在 `document/` 下新增实施说明文件（本文件）。

下一步建议（优先级排序）：
1. 把 `tools/verify_rules.ps1` 集成到 CI（GitHub Actions / Azure DevOps），在 PR 检查阶段运行并阻止规则违规的合并。需要我生成一个 GH Actions workflow 吗？
2. 把脚本从 PowerShell 扩展为跨平台 Node.js 脚本（便于在 Linux CI 上运行）。
3. 针对脚本标记的项（例如 `@RequestBody` 未标注 `@Validated`），逐条人工修复，并在 `document/` 中记录修复记录和教训。

如何运行校验脚本（本地 Windows PowerShell）：

```powershell
# 在仓库根目录下运行（PowerShell）
powershell -ExecutionPolicy Bypass -File .\tools\verify_rules.ps1
```

备注：脚本为辅助检测工具，不能替代人工审查。对于脚本报告的条目，请人工确认并修复。