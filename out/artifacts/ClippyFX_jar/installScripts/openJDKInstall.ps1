$source = 'https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_windows-x64_bin.zip'
"Downloading openjdk from [$source] please wait..."
$destination = 'openjdk-17.0.1.zip'
Start-BitsTransfer -Source $source -Destination $destination
"Extracting openjdk to [$destination] please wait..."
Expand-Archive -LiteralPath $destination -DestinationPath 'openjdk-TEMP'
"Moving openjdk to [C:\Program Files\openjdk-17.0.1] please wait..."
Move-Item -Path 'openjdk-TEMP\jdk-17.0.1' -Destination 'C:\Program Files\openjdk-17.0.1' -Force
"Cleaning up temporary files..."
Remove-Item -LiteralPath 'openjdk-TEMP' -Force -Recurse
Remove-Item -LiteralPath 'openjdk-17.0.1.zip' -Force
"Adding openjdk to the system path..."
sleep 0.75
[Environment]::SetEnvironmentVariable(
    "Path",
    [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::Machine) + ";C:\Program Files\openjdk-17.0.1\bin",
    [EnvironmentVariableTarget]::Machine)