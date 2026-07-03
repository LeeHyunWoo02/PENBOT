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

$path = $null
foreach ($name in @("file_path", "path")) {
    if ($toolInput.PSObject.Properties.Name -contains $name) {
        $path = [string]$toolInput.$name
        break
    }
}

if ([string]::IsNullOrWhiteSpace($path)) {
    exit 0
}

$normalized = $path -replace "\\", "/"

if ($normalized -match "src/main/java/Project/PENBOT/Booking/") {
    [Console]::Error.WriteLine("Harness reminder: booking code changed. Run: .\gradlew.bat test --tests Project.PENBOT.Booking.Service.BookingServiceTest")
}

if ($normalized -match "src/main/java/Project/PENBOT/Host/" -or
    $normalized -match "SecurityConfig\.java$" -or
    $normalized -match "LoginFilter\.java$") {
    [Console]::Error.WriteLine("Harness reminder: admin/security code changed. Run host/security focused tests before finishing.")
}

if ($normalized -match "src/main/java/Project/PENBOT/(OpenAi|Verify)/") {
    [Console]::Error.WriteLine("Harness reminder: external API code changed. Prefer mock-based tests; do not call real OpenAI/CoolSMS APIs during tests.")
}

exit 0
