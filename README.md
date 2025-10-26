# Realtek Driver Cleaner

So I got a new headset for my PC and the audio was completely messed up. Crackling, popping, weird volume issues - the whole nine yards. Turns out Windows had installed generic Realtek audio drivers that were conflicting with my motherboard's actual audio setup.

I tried uninstalling them through Device Manager (even the hidden ones), but some of these drivers are *stubborn*. They just wouldn't go away completely. Every time I thought I got them all, there'd be more hiding in the driver store causing problems.

This tool nukes all the Realtek drivers from your system so you can start fresh with the correct motherboard-specific drivers. Problem solved. 🎧

## What It Does

- Finds every single Realtek audio driver hiding on your system
- Deletes them all (with your confirmation, obviously)
- Auto-detects your motherboard and helps you find the right drivers
- Optionally restarts your PC when you're done

## Why You Might Need This

- Just got new audio equipment and it sounds terrible
- Windows installed generic Realtek drivers instead of your motherboard's specific ones
- Tried removing drivers manually but they keep coming back
- Audio crackling, popping, or just not working right
- Device Manager uninstall didn't actually remove everything

## How to Use

1. Download the JAR file from [Releases](../../releases)
2. **Right-click** it and select **"Run as administrator"** (this is important!)
3. Click "Scan for Drivers" to see what Realtek stuff is lurking
4. Click "Delete All Drivers" to nuke them
5. Let it help you find your motherboard's actual audio drivers
6. Restart and install the proper drivers

## Requirements

- Windows 10 or 11
- Java 8 or newer ([download here](https://www.java.com/download/) if you don't have it)
- Administrator privileges (just right-click → run as admin)

## ⚠️ Heads Up

This permanently deletes drivers from your system. Make sure you:
- Know how to get your motherboard's audio drivers (the tool will help)
- Save any work before restarting
- Maybe create a system restore point if you're cautious

The tool walks you through everything, so don't stress about it.

## Building It Yourself

If you want to compile from source:
```bash
# Compile
javac -d bin src/RealtekCleanerGUI.java

# Create JAR
jar cfe RealtekCleanerGUI.jar RealtekCleanerGUI -C bin .
```

Or just run `build.bat` if you're on Windows.

## Contributing

Found a bug? Want to add a feature? Pull requests welcome! Open an issue first if it's something big.

## License

MIT License - do whatever you want with it.

---

Made this out of frustration with audio drivers. Hope it helps someone else avoid the same headache.
