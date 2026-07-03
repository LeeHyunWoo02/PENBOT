$ErrorActionPreference = "Stop"

$inputJson = [Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($inputJson)) {
    exit 0
}

try {
    $payload = $inputJson | ConvertFrom-Json
} catch {
    exit 0
}

$toolInput = $payload.tool_input
if ($null -eq $toolInput) {
    exit 0
}

$candidatePaths = @()
foreach ($name in @("file_path", "path")) {
    if ($toolInput.PSObject.Properties.Name -contains $name) {
        $candidatePaths += [string]$toolInput.$name
    }
}

if ($candidatePaths.Count -eq 0) {
    exit 0
}

$protectedPathPatterns = @(
    "\.env$",
    "env\.properties$",
    "compose\.env$",
    "application\.properties$",
    "compose\.yml$",
    "Dockerfile$",
    "build\.gradle$",
    "gradlew$",
    "gradlew\.bat$",
    "gradle[\\/]+wrapper[\\/]+gradle-wrapper\.jar$",
    "SecurityConfig\.java$",
    "LoginFilter\.java$"
)

$secretValuePatterns = @(
    "sk-[A-Za-z0-9_-]{20,}",
    "OPENAI_API_KEY\s*=\s*[^`$][^\s]+",
    "COOLSMS_API_KEY\s*=\s*[^`$][^\s]+",
    "COOLSMS_API_SECRET\s*=\s*[^`$][^\s]+",
    "MYSQL_ROOT_PASSWORD\s*=\s*[^`$][^\s]+",
    "GOOGLE_PLACES_API_KEY\s*=\s*[^`$][^\s]+"
)

$content = ""
foreach ($name in @("content", "new_string")) {
    if ($toolInput.PSObject.Properties.Name -contains $name -and $null -ne $toolInput.$name) {
        $content += "`n" + [string]$toolInput.$name
    }
}

foreach ($pattern in $secretValuePatterns) {
    if ($content -match $pattern) {
        [Console]::Error.WriteLine("Blocked: this edit appears to contain a real secret value. Use environment-variable placeholders instead.")
        exit 2
    }
}

foreach ($path in $candidatePaths) {
    $normalized = $path -replace "\\", "/"
    foreach ($pattern in $protectedPathPatterns) {
        if ($normalized -match $pattern) {
            [Console]::Error.WriteLine("Guard: editing protected project file '$path'. Continue only when this change is intentional and verified.")
            exit 0
        }
    }
}

exit 0
