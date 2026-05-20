# rule to compile kt file to jar file
test: Test.kt
	kotlinc Test.kt -include-runtime -d Test.jar

run: Test.jar
	java -jar Test.jar

clean:
	rm -rf *.jar
