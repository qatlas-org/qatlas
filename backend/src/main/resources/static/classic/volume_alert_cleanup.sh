#!/bin/sh
# ------------------------
# CONFIGURATION
# ------------------------
VOLUME_PATH="/app"
CLEAN_PATH="/app/data/attachments"
EMAIL1="reddy.busa@luxtrust.com"
EMAIL2="bvenku1982@gmail.com"
WARN_THRESHOLD=60
CLEAN_THRESHOLD=70
TARGET_USAGE=50

# ------------------------
# GET CURRENT USAGE (Robust Version)
# ------------------------
# -Ph ensures the output stays on one line and follows standard column spacing
CURRENT_USAGE=$(df -Ph "$VOLUME_PATH" | awk 'NR==2 {gsub(/%/,"",$5); print $5}')

# Safety check: Ensure CURRENT_USAGE is actually a number
case $CURRENT_USAGE in
    ''|*[!0-9]*) echo "Error: Could not determine disk usage." ; exit 1 ;;
esac

echo "Current disk usage: $CURRENT_USAGE%"

# ------------------------
# SEND EMAIL IF ABOVE WARN_THRESHOLD
# ------------------------
if [ "$CURRENT_USAGE" -gt "$WARN_THRESHOLD" ]; then
    echo "Disk usage warning: $CURRENT_USAGE% used on $VOLUME_PATH" | mail -s "Docker Volume Warning" "$EMAIL1,$EMAIL2"
    echo "Email notification triggered."
fi

# ------------------------
# DELETE OLDEST FOLDERS IF ABOVE CLEAN_THRESHOLD
# ------------------------
if [ "$CURRENT_USAGE" -gt "$CLEAN_THRESHOLD" ]; then
    echo "Disk usage above $CLEAN_THRESHOLD%, starting cleanup..."
    
    # Check if directory exists before trying to list it
    if [ ! -d "$CLEAN_PATH" ]; then
        echo "Error: Cleanup path $CLEAN_PATH does not exist."
        exit 1
    fi

    while [ "$CURRENT_USAGE" -gt "$TARGET_USAGE" ]; do
        # Finds oldest directory specifically
        OLDEST=$(ls -trd "$CLEAN_PATH"/*/ 2>/dev/null | head -n 1 | sed 's:/$::')
        
        if [ -z "$OLDEST" ]; then
            echo "No more folders to delete."
            break
        fi
        
        SIZE_MB=$(du -sm "$OLDEST" | awk '{print $1}')
        echo "Deleting oldest folder: $OLDEST (~${SIZE_MB}MB)"
        rm -rf "$OLDEST"
        
        # Refresh usage
        CURRENT_USAGE=$(df -Ph "$VOLUME_PATH" | awk 'NR==2 {gsub(/%/,"",$5); print $5}')
        echo "Disk usage now: $CURRENT_USAGE%"
    done
    echo "Cleanup complete. Final disk usage: $CURRENT_USAGE%"
fi