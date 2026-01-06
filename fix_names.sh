#!/bin/bash
# Rename the guide file
mv PACKAGE_LOGGER.md PACKET_LOGGER.md 2>/dev/null || true

# Content replacements
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/Package Logger/Packet Logger/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/logPackages/logPackets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/selectedS2CPackages/selectedS2CPackets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/selectedC2SPackages/selectedC2SPackets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/packages-/packets-/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/allPackages/allPackets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/selectedPackages/selectedPackets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/getShortPackageName/getShortPacketName/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/Search packages/Search packets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/available packages/available packets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/selected packages/selected packets/g' {} +
find . -type f \( -name "*.java" -o -name "*.json" -o -name "*.gradle" -o -name "*.properties" -o -name "*.md" \) -exec sed -i 's/Fabric-Package-Logger/Fabric-Packet-Logger/g' {} +

# Specific constructor param rename (might be missed by bulk)
sed -i 's/List<String> packages/List<String> packets/g' src/client/java/dev/redstone/packetlogger/screen/widget/DualListSelectorWidget.java 2>/dev/null || true
sed -i 's/packages)/packets)/g' src/client/java/dev/redstone/packetlogger/screen/widget/DualListSelectorWidget.java 2>/dev/null || true
sed -i 's/pkg/pkt/g' src/client/java/dev/redstone/packetlogger/screen/widget/DualListSelectorWidget.java 2>/dev/null || true

git add -A
git commit -m "Finale Namenskorrektur/Korrektur: Alle verbleibenden 'package' zu 'packet' geändert (Variablen, Kommentare, Dokumentation)"
