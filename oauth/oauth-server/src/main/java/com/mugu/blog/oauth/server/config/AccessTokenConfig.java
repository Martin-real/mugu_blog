package com.mugu.blog.oauth.server.config;
import com.mugu.blog.core.model.oauth.OAuthConstant;
import com.mugu.blog.oauth.server.model.SecurityUser;
import com.mugu.blog.oauth.server.model.TokenConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.LinkedHashMap;

/**
 * @author  公众号：码猿技术专栏
 * 令牌的配置
 */
@Configuration
public class AccessTokenConfig {

    @Autowired
    private TokenConstant tokenConstant;

    /**
     * 令牌的存储策略
     */
    @Bean
    public TokenStore tokenStore() {
        //使用JwtTokenStore生成JWT令牌
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * JwtAccessTokenConverter
     * TokenEnhancer的子类，在JWT编码的令牌值和OAuth身份验证信息之间进行转换。
     * TODO：后期可以使用非对称加密
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter converter = new JwtAccessTokenEnhancer();
        // 设置秘钥
        converter.setSigningKey(tokenConstant.getSignKey());
        return converter;
    }

    /**
     * JWT令牌增强，继承JwtAccessTokenConverter
     * 将业务所需的额外信息放入令牌中，这样下游微服务就能解析令牌获取
     */
    public static class JwtAccessTokenEnhancer extends JwtAccessTokenConverter {
        /**
         * 重写enhance方法，在其中扩展
         */
        @Override
        public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
            //获取userDetailService中查询到用户信息
            SecurityUser user=(SecurityUser)authentication.getUserAuthentication().getPrincipal();
            //将额外的信息放入到LinkedHashMap中
            LinkedHashMap<String,Object> extendInformation=new LinkedHashMap<>();
            //设置用户的userId
            extendInformation.put(OAuthConstant.USER_ID,user.getUserId());
            extendInformation.put(OAuthConstant.GENDER,user.getGender());
            extendInformation.put(OAuthConstant.NICK_NAME,user.getNickname());
            extendInformation.put(OAuthConstant.AVATAR,user.getAvatar());
            extendInformation.put(OAuthConstant.MOBILE,user.getEmail());
            extendInformation.put(OAuthConstant.EMAIL,user.getUserId());
            //添加到additionalInformation
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(extendInformation);
            return super.enhance(accessToken, authentication);
        }
    }
}