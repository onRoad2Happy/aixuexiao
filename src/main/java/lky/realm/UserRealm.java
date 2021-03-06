package lky.realm;

import java.util.HashSet;

import javax.annotation.Resource;

import lky.entity.User;
import lky.service.UserService;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;

public class UserRealm extends AuthorizingRealm{
	@Resource
	private UserService userService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		String username = (String)principals.getPrimaryPrincipal();
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		authorizationInfo.setRoles(new HashSet<String>(userService.findRoles(username)));
		authorizationInfo.setStringPermissions(new HashSet<String>(userService.findPermission(username)));
		return authorizationInfo;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		String username = (String)token.getPrincipal();
		User user = userService.findByUsername(username);
		if (user==null) {
			throw new UnknownAccountException();
		}
		
		if (Boolean.TRUE.equals(user.getLocked())) {
			throw new LockedAccountException();
		}
		SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(user.getUsername(),
				user.getPassword(),
				ByteSource.Util.bytes(user.getCredentialsSalt()),getName());
		return authenticationInfo;
	}
	
	public void clearAllCachedAuthorizationInfo(){
		getAuthenticationCache().clear();
	}
	
	public void clearAllCachedAuthenticationInfo(){
		getAuthenticationCache().clear();
	}
	public void clearAllCache(){
		clearAllCachedAuthorizationInfo();
		clearAllCachedAuthenticationInfo();
	}
}
