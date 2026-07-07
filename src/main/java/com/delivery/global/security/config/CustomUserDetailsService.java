// package com.delivery.global.security;
//
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
//
// import java.util.List;
// import java.util.stream.Collectors;
//
// @Service
// @RequiredArgsConstructor
// public class CustomUserDetailsService implements UserDetailsService {
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user =
//                userRepository
//                        .findByUsername(username)
//                        .orElseThrow(
//                                () ->
//                                        new UsernameNotFoundException(
//                                                "User not found with username: " + username));
//
//        List<GrantedAuthority> authorities =
//                user.getRoles().stream()
//                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
//                        .collect(Collectors.toList());
//
//        return new CustomUserDetails(user.getId(), username, user.getPassword(), authorities);
//    }
// }
