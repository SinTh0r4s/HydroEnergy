wget http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10-src.zip
unzip -o forge-1.7.10-10.13.4.1614-1.7.10-src gradle/wrapper/gradle-wrapper.jar eclipse/* gradlew gradlew.bat
rm forge-1.7.10-10.13.4.1614-1.7.10-src.zip
cp -f setup/build.gradle .
cp -f setup/gradle-wrapper.properties gradle/wrapper/