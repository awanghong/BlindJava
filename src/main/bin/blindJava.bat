@echo off
set home=%~dp0

set conf_dir=%home%..\conf
set lib_dir=%home%..\lib\*
set log_dir=%home%..\logs

java -classpath %conf_dir%;%lib_dir% com.gitee.freakchicken.blindjava.handler.BlindVariable
java -classpath %conf_dir%;%lib_dir% com.gitee.freakchicken.blindjava.handler.BlindMethod
java -classpath %conf_dir%;%lib_dir% com.gitee.freakchicken.blindjava.handler.BlindClass
java -classpath %conf_dir%;%lib_dir% com.gitee.freakchicken.blindjava.handler.BlindNote

pause