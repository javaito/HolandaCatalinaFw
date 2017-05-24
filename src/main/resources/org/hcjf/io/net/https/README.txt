
keytool -genkey -alias keyhcjf -keyalg RSA -keypass hcjfkeypassword -storepass hcjfkeystorepassword -keystore keystore.jks

keytool -export -alias keyhcjf -storepass hcjfkeystorepassword -file server.cer -keystore keystore.jks

keytool -import -v -trustcacerts -alias keyhcjf -file server.cer -keystore cacerts.jks -keypass hcjfkeypassword -storepass hcjfkeystorepassword