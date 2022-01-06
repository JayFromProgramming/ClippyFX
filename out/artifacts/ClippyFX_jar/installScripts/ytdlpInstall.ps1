$fullInstall=$args[0]
$source = 'https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe'
$destination = 'yt-dlp.exe'
if ("-sys" -eq $fullInstall) {
    "Checking for system installation"
    if (Test-Path -Path 'C:\Program Files\ytdlp\yt-dlp.exe'){
    "yt-dlp is already installed"
    exit 0
  } else {
    "Downloading yt-dlp from [$source] please wait..."
    Start-BitsTransfer -Source $source -Destination $destination
    "Installing yt-dlp..."
    New-Item -Path 'C:\Program Files\' -Name 'ytdlp' -ItemType 'directory'
    Move-Item -Path 'yt-dlp.exe' -Destination 'C:\Program Files\ytdlp\yt-dlp.exe'
    "Adding yt-dlp to the system path"
    [Environment]::SetEnvironmentVariable(
        "Path",
        [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::Machine) + ";C:\Program Files\ytdlp\",
        [EnvironmentVariableTarget]::Machine)
    "yt-dlp is now installed"
    exit 0
  }
} else {
    "Checking for local installation"
    if (Test-Path -Path 'yt-dlp.exe'){
        "yt-dlp is already installed"
        exit 0
    } else {
        "Downloading yt-dlp from [$source] please wait..."
        Start-BitsTransfer -Source $source -Destination $destination
        exit 0
    }
}