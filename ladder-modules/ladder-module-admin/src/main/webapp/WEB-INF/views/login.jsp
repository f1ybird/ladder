<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<!DOCTYPE HTML>
<html>
<%@ include file="common/common.jsp" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>登录 - ${title}</title>
</head>
<script type="text/javascript">
	// 提交表单
	function mySubmit(){
	    var username = $.trim($("#username").val());
	    var pwd = $.trim($("#pwd").val());
	    if(!username){
	        layer.alert("请输入用户名");
	        return;
		}

		if(!pwd){
	        layer.alert("请输入密码");
	        return;
		}

		$("#mySubmit").submit();
	}

	$(function () {
        document.onkeydown = function(e){
            var ev = document.all ? window.event : e;
            if(ev.keyCode == 13){
                mySubmit();
            }
        }
    });

</script>

<body>
	<div class="J_loginMain">
		<div class="l_inner">
			<form action="${ctx}/do_login" method="post" id="mySubmit">
				<div class="i_main">
					<div class="m_txt">
						<c:choose>
							<c:when test="${not empty msg}">${msg}</c:when>
							<c:otherwise>${title}</c:otherwise>
						</c:choose>
					</div>
					<div class="m_input">
						<input name="username" id="username" placeholder="请输入账号" type="text" />
					</div>
					<div class="m_input">
						<input name="pwd" id="pwd" placeholder="请输入密码" type="password" />
					</div>
					<div class="m_btn">
						<a href="javascript:mySubmit();">登录</a>
					</div>
				</div>
			</form>
		</div>
	</div>

	<%@ include file="common/footer.jsp" %>
</body>
</html>