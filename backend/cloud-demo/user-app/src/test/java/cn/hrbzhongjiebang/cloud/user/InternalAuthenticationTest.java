package cn.hrbzhongjiebang.cloud.user;
import cn.hrbzhongjiebang.cloud.contracts.InternalAuthenticationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;
class InternalAuthenticationTest {
 @Test void forgedUserHeaderCannotBypassInternalAuthentication() throws Exception {
  var filter=new InternalAuthenticationConfiguration().internalAuthentication("test-internal-012345678901234567890");
  for(String supplied:new String[]{"", "wrong", "test-internal-012345678901234567890"}) {
   var request=new MockHttpServletRequest("GET","/internal/users/1");request.addHeader("X-User-Id","1");request.addHeader("X-Internal-Token",supplied);
   var response=new MockHttpServletResponse();var reached=new java.util.concurrent.atomic.AtomicBoolean();
   filter.doFilter(request,response,(req,res)->reached.set(true));
   assertEquals(supplied.startsWith("test-internal"),reached.get());
   if(!reached.get())assertEquals(401,response.getStatus());
  }
 }
}
