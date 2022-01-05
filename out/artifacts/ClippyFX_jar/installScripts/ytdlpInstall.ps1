$source = 'https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe'
$destination = 'yt-dlp.exe'
Start-BitsTransfer -Source $source -Destination $destination