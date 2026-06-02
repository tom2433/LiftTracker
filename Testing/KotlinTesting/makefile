# rule to compile kt file to jar file
test: Test.kt
	kotlinc Test.kt -include-runtime -d Test.jar

r: Test.jar
	java -jar Test.jar

c:
	rm -rf *.jar *.class
