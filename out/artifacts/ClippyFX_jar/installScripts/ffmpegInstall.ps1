$source = 'https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip'
$destination = 'ffmpeg.zip'
Start-BitsTransfer -Source $source -Destination $destination
Expand-Archive -LiteralPath 'ffmpeg.zip' -DestinationPath 'ffmpeg-extracted' -Force
$path = Get-ChildItem 'ffmpeg-extracted' |
        Where-Object {$_.PSIsContainer} |
        Foreach-Object {$_.Name}
$ffmpegpath = 'ffmpeg-extracted\'+$path+'\bin\ffmpeg.exe'
$ffprobepath = 'ffmpeg-extracted\'+$path+'\bin\ffprobe.exe'
Move-Item -Path $ffmpegpath -Destination '.\ffmpeg.exe'
Move-Item -Path $ffprobepath -Destination '.\ffprobe.exe'
Remove-Item -LiteralPath 'ffmpeg-extracted' -Force -Recurse
Remove-Item -LiteralPath 'ffmpeg.zip' -Force