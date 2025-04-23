# Unix, Linux, MacOS, WSL, Bash
DECOMPILED_DIR="decompiled"

mkdir -p "$DECOMPILED_DIR"

mv *.class "$DECOMPILED_DIR"

echo "Decompiled files moved to $DECOMPILED_DIR/"