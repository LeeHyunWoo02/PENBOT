$ErrorActionPreference = "Stop"

try {
    $changed = git diff --name-only
} catch {
    exit 0
}

if ([string]::IsNullOrWhiteSpace($changed)) {
    exit 0
}

$files = $changed -split "`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }

$javaChanged = $files | Where-Object { $_ -match "^src/main/java/.*\.java$" }
$bookingChanged = $files | Where-Object { $_ -match "^src/main/java/Project/PENBOT/Booking/" }
$securityChanged = $files | Where-Object { $_ -match "SecurityConfig\.java$|LoginFilter\.java$|^src/main/java/Project/PENBOT/Host/" }
$externalApiChanged = $files | Where-Object { $_ -match "^src/main/java/Project/PENBOT/(OpenAi|Verify)/" }

if ($bookingChanged) {
    [Console]::Error.WriteLine("Stop reminder: booking files changed. Recommended check: .\gradlew.bat test --tests Project.PENBOT.Booking.Service.BookingServiceTest")
}

if ($securityChanged) {
    [Console]::Error.WriteLine("Stop reminder: host/security files changed. Verify allowed and forbidden access paths.")
}

if ($externalApiChanged) {
    [Console]::Error.WriteLine("Stop reminder: OpenAi/Verify files changed. Verify with mocks and avoid real external API calls.")
}

if ($javaChanged) {
    [Console]::Error.WriteLine("Stop reminder: Java files changed. Run .\gradlew.bat test when practical before handing off.")
}

exit 0
