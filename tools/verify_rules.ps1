<#
PowerShell 验证脚本：tools/verify_rules.ps1
用途：在本地或 CI 中运行，对仓库规则进行静态检测。
退出码：0=通过, 非0=失败
#>

$errors = @()

Write-Output "[verify_rules] 开始检查..."

# 1. 检查 backend/pom.xml 中是否包含 fastjson2
$pom = Join-Path $PSScriptRoot '..\backend\pom.xml'
if (Test-Path $pom) {
    $pomContent = Get-Content $pom -Raw
    if ($pomContent -notmatch 'fastjson' -and $pomContent -notmatch 'fastjson2') {
        $errors += "backend/pom.xml 未检测到 fastjson2 依赖，请使用 Fastjson2（或在规则中说明替代库）。"
    } else {
        Write-Output "[OK] backend/pom.xml 包含 fastjson2 依赖或关键字。"
    }
} else {
    Write-Output "[WARN] 找不到 backend/pom.xml，跳过 fastjson2 检查。"
}

# 2. 检查前端是否使用 TypeScript（是否存在 tsconfig.json）
$frontTs = Test-Path (Join-Path $PSScriptRoot '..\front-ui\tsconfig.json')
$adminTs = Test-Path (Join-Path $PSScriptRoot '..\admin-ui\tsconfig.json')
if (-not $frontTs) { $errors += "front-ui 缺少 tsconfig.json，前端应使用 TypeScript。" } else { Write-Output "[OK] front-ui 存在 tsconfig.json。" }
if (-not $adminTs) { $errors += "admin-ui 缺少 tsconfig.json，管理端应使用 TypeScript。" } else { Write-Output "[OK] admin-ui 存在 tsconfig.json。" }

# 3. 查找 front-ui 下的 .js 源文件（建议全部迁移为 .ts）
$jsFiles = Get-ChildItem -Path (Join-Path $PSScriptRoot '..\front-ui') -Recurse -Include "*.js" -ErrorAction SilentlyContinue
if ($jsFiles) {
    $errors += "front-ui 存在 .js 文件，请尽量迁移为 TypeScript (.ts)。发现文件数: $($jsFiles.Count)"
} else { Write-Output "[OK] front-ui 未发现 .js 源文件。" }

# 4. 简易扫描 Java 控制器中 @RequestBody 使用但未标注 @Validated 的可疑位置
$javaControllers = Get-ChildItem -Path (Join-Path $PSScriptRoot '..\backend\src\main\java') -Recurse -Include "*Controller.java" -ErrorAction SilentlyContinue
foreach ($f in $javaControllers) {
    $text = Get-Content $f.FullName -Raw
    $matches = Select-String -InputObject $text -Pattern "@RequestBody" -AllMatches
    foreach ($m in $matches) {
        # 检查前后 2 行是否包含 @Validated
        $lineNum = $m.LineNumber
        $lines = $text -split "\r?\n"
        $start = [Math]::Max(0, $lineNum-3)
        $end = [Math]::Min($lines.Length-1, $lineNum+2)
        $snippet = $lines[$start..$end] -join "\n"
        if ($snippet -notmatch "@Validated") {
            $errors += "可疑: $($f.FullName) 在第 $lineNum 行使用 @RequestBody，但局部未找到 @Validated 注解，建议人工复核。"
        }
    }
}
Write-Output "[verify_rules] 检查完成。"

if ($errors.Count -gt 0) {
    Write-Output "发现问题："
    $errors | ForEach-Object { Write-Output " - $_" }
    exit 2
} else {
    Write-Output "所有检查通过。"
    exit 0
}
