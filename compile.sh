set -e
mkdir out/ &> /dev/null
mkdir out/production/ &> /dev/null
mkdir out/production/sdis1920-t1g02 &> /dev/null
javac -d out/production/sdis1920-t1g02 $(find . -name "*.java")