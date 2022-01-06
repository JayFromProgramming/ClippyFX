$fullInstall=$args[0]

function Download-FFmpeg{
    $source = 'https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip'
    "Downloading FFmpeg from [" + $source + "] please wait..."
    $destination = 'ffmpeg.zip'
    Start-BitsTransfer -Source $source -Destination $destination
    "Unzipping FFmpeg please wait..."
    Expand-Archive -LiteralPath 'ffmpeg.zip' -DestinationPath 'ffmpeg-extracted' -Force
    "Extracting FFmpeg please wait..."
}

if ("-sys" -eq $fullInstall) {
    "Checking for preexisting system installation of FFmpeg"
    if (Test-Path -Path 'C:\Program Files\ffmpeg\bin\ffmpeg.exe') {
        "FFmpeg is already installed"
        exit 0
    } else {
        Write-Host "FFmpeg is not installed, installing now"
    }
    Download-FFmpeg
    $path = Get-ChildItem 'ffmpeg-extracted' |
                Where-Object {$_.PSIsContainer} |
                Foreach-Object {$_.Name}
        $ffmpegpath = 'ffmpeg-extracted\'+$path+'\bin\ffmpeg.exe'
        $ffprobepath = 'ffmpeg-extracted\'+$path+'\bin\ffprobe.exe'
        "Extracting FFmpeg please wait..."
    "Moving FFmpeg to program files"
    New-Item -Path 'C:\Program Files\' -Name 'ffmpeg' -ItemType 'directory'
    New-Item -Path 'C:\Program Files\ffmpeg\' -Name 'bin' -ItemType 'directory'
    Move-Item -Path $ffmpegpath -Destination 'C:\Program Files\ffmpeg\bin\ffmpeg.exe' -Force
    Move-Item -Path $ffprobepath -Destination 'C:\Program Files\ffmpeg\bin\ffprobe.exe' -Force
    "Adding FFmpeg to the system path"
    [Environment]::SetEnvironmentVariable(
        "Path",
        [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::Machine) + ";C:\Program Files\ffmpeg\bin\",
        [EnvironmentVariableTarget]::Machine)
    "Cleaning up"
    Remove-Item -LiteralPath 'ffmpeg.zip' -Force
    Remove-Item -LiteralPath 'ffmpeg-extracted' -Force -Recurse
    "FFmpeg has been installed"
    exit 0
} else {
    "Checking for preexisting local installation of FFmpeg"
    if (Test-Path -Path "ffmpeg.exe") {
        "FFmpeg is already installed"
        exit 0
    } else {
        Write-Host "FFmpeg is not installed, installing now"
    }
    Download-FFmpeg
    $path = Get-ChildItem 'ffmpeg-extracted' |
                Where-Object {$_.PSIsContainer} |
                Foreach-Object {$_.Name}
        $ffmpegpath = 'ffmpeg-extracted\'+$path+'\bin\ffmpeg.exe'
        $ffprobepath = 'ffmpeg-extracted\'+$path+'\bin\ffprobe.exe'
        "Extracting FFmpeg please wait..."
    "Moving FFmpeg to current directory"
    Move-Item -Path $ffmpegpath -Destination '.\ffmpeg.exe' -Force
    Move-Item -Path $ffprobepath -Destination '.\ffprobe.exe' -Force
    "Cleaning up"
    Remove-Item -LiteralPath 'ffmpeg.zip' -Force
    Remove-Item -LiteralPath 'ffmpeg-extracted' -Force -Recurse
    "FFmpeg has been installed"
}
