echo %1
echo %2
java -jar dist\GerberToGcode.jar -penSize=0.4 -dx=80.4 -dy=80.5 -show %1 %2