package br.com.devd2.meshstorageserver.config;

import br.com.devd2.meshstorageserver.props.MeshUploadProps;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MeshUploadProps.class)
public class PropsConfig {}
