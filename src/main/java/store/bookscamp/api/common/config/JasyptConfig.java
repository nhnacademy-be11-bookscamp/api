package store.bookscamp.api.common.config;

import org.jasypt.encryption.StringEncryptor; // <-- 인터페이스로 변경
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfig {

    @Value("${JASYPT_ENCRYPTOR_PASSWORD}")
    private String encryptKey;

    @Bean(name = "jasyptEncryptor")
    public StringEncryptor jasyptEncryptor() { // <-- 반환 타입을 StringEncryptor로 변경
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        config.setPassword(encryptKey); // @Value로 주입받은 키 설정
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config); // 암호화기에 config를 설정

        return encryptor; // <-- 실제 암호화기(encryptor)를 반환
    }
}