package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.models.UserModel;
import io.dy.gamecenter.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class UserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserModel user = authRepository.findByUserId(userId);
        if (user == null) {
            return null;
//            throw new UsernameNotFoundException("User not found");
        }
        List<GrantedAuthority> authorities = getUserAuthorities(user.getRoles());
        return new User(user.getUserId(), "", authorities);
    }

    public List<GrantedAuthority> getUserAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }
}
