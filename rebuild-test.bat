call gradlew clean
call gradlew test --tests java11.StringConcatenationTest
call start chrome.exe "C:\programming\jpf-core\build\reports\tests\test\index.html"