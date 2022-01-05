$source = 'https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_windows-x64_bin.zip'
$destination = 'openjdk-17.0.1.zip'
Start-BitsTransfer -Source $source -Destination $destination
Expand-Archive -LiteralPath $destination -DestinationPath 'openjdk-TEMP'
Move-Item -Path 'openjdk-TEMP\jdk-17.0.1' -Destination 'C:\Program Files\openjdk-17.0.1' -Force
Remove-Item -LiteralPath 'openjdk-TEMP' -Force -Recurse
Remove-Item -LiteralPath 'openjdk-17.0.1.zip' -Force
[Environment]::SetEnvironmentVariable(
    "Path",
    [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::Machine) + ";C:\Program Files\openjdk-17.0.1\bin",
    [EnvironmentVariableTarget]::Machine)