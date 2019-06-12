# fiscocc-onbc fiscocc项目存证上链服务

## 1.jar包信息

- 基于上链服务：master-20180516-f89fa351bb311dce3d74b6d313db1e5349979096
- 公网web3sdk V1.2.4


## 2.数据库密钥生成方式

- 命令：java -cp druid-1.1.10.jar com.alibaba.druid.filter.config.ConfigTools [数据库密码]

```sh
- 例子：java -cp druid-1.1.10.jar com.alibaba.druid.filter.config.ConfigTools root
privateKey:MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAijf/wPgFFD7Mlp+j3wE1r1dLbxc18GSLiwc5KI64TXJulR1KmUNW3iiIeYZKORvx8sSnV9nGaJGXyZaAPYCaNQIDAQABAkA6C15KKV3orJ66OnxU8GsdIWm6U2MBexfm4LeuQpE/ZEgq8YQiKALML7LdSK4ZSLrUBfwUOo5S37Y0LI78+ebZAiEA4tohIIbkVNWRv3O8BNhNk6FQsFyJE7KN2PjQKCpVbUsCIQCb+nGdlStpgNYLmD1RzjRVAm/tSZLKXHB9gBqRqArmfwIgMfrLB6aQkdxH8z1ldE/Pr7H/3AtXLB7Pv7j564+AKMcCIGMAQi7wKF7NrI4tcfZDeInggyRMR4Rzyd6Oec6rp0eHAiEAnuA7Mbf2mIX2ynhJfVzyJuAk6yoOu+S5lhny0Ds+mPU=
publicKey:MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIo3/8D4BRQ+zJafo98BNa9XS28XNfBki4sHOSiOuE1ybpUdSplDVt4oiHmGSjkb8fLEp1fZxmiRl8mWgD2AmjUCAwEAAQ==
password:a9hJkkVn8CELCIepavJKVNXv0wxhXDvmDZWVvzsKsuYY3IMXdh7rAb4j2UTEdlmBFRoM3e1T+eUny3HzNGkhtA==
```

- a.通过以上命令会生成:privateKey(私钥)、publicKey(公钥)、password(加密密码);
- b.生成之后，修改application.propertites配置文件属性：spring.datasource.password=password, public-key=publicKey;

## 3.生成client.keystore
```sh
FISCO-BCOS中client.keystore 的生成方法
1、client.keystore是用做web3sdk的SSL证书。
2、在web3sdk V1.0.0中，client.keystore里面有两个证书，一个client证书，一个ca证书。client.keystore的密码必须为“123456”,keystore中有私钥的client证书的密码也必须为“123456”。
client.keystore生成方式:
（1）keytool -import -trustcacerts -alias ca -file ca.crt -keystore client.keystore
（2）openssl pkcs12 -export -name client -in server.crt -inkey server.key -out keystore.p12
（3）keytool -importkeystore -destkeystore client.keystore -srckeystore keystore.p12 -srcstoretype pkcs12 -alias client
（4）Attention！ Password must be ”123456”
```

## 4.生成签名公私钥

##### 私钥是由linux系统下java JDK/bin中的keytool工具生成的（生成命令如下）:
```sh
参数说明：
-genkey：创建证书
-alias：证书的别名。在一个证书库文件中，别名是唯一用来区分多个证书的标识符
-keyalg：密钥的算法，非对称加密的话就是RSA
-keystore：证书库文件保存的位置和文件名。如果路径写错的话，会出现报错信息。如果在路径下，证书库文件不存在，那么就会创建一个
-keysize：密钥长度，一般都是1024
-validity：证书的有效期，单位是天。比如36500的话，就是100年
-genkeypair:生成一对非对称密钥
注意：
1.密钥库的密码至少必须6个字符，可以是纯数字或者字母或者数字和字母的组合等等
2."名字与姓氏"应该是输入域名，而不是我们的个人姓名，其他的可以不填

例子：keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA -validity 3650 -storetype JKS -keystore user.jks -storepass 123456
```
##### 公钥生成
```sh
进入部署目录：
windows环境输入命令：(如果window环境运行异常，请使用工具类方法：org.bcos.fiscocc.onbc.util.SignPulibcKeyUtil)
java -cp 'conf/;apps/*;lib/*;' getPublicKey keyStoreFileName keyStorePassword keyPassword 
例子：java -cp 'conf/;apps/*;lib/*;' org.bcos.fiscocc.onbc.util.SignPulibcKeyUtil getPublicKey szt.jks 123456 123456

linux环境输入命令：
java -cp 'conf/:apps/*:lib/*' getPublicKey keyStoreFileName keyStorePassword keyPassword 
例子：java -cp 'conf/:apps/*:lib/*' org.bcos.fiscocc.onbc.util.SignPulibcKeyUtil getPublicKey szt.jks 123456 123456
```

## 5.文件转换成hash
```
进入部署目录：
windows环境输入命令：(如果window环境运行异常，请使用工具类方法：org.bcos.fiscocc.onbc.util.FileHash)
java -cp 'conf/;apps/*;lib/*;' file
例子：java -cp 'conf/;apps/*;lib/*;' org.bcos.fiscocc.onbc.util.FileHash szt.jks

linux环境输入命令：
java -cp 'conf/:apps/*:lib/*' file
例子：java -cp 'conf/:apps/*:lib/*' org.bcos.fiscocc.onbc.util.FileHash szt.jks
```

