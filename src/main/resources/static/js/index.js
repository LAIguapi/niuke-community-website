$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");
    //发送AJAX请求钱，将CSRF令牌设置到请求头信息
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("header");
    // $(document).ajaxSend(function (e, xhr, options) {
    //     xhr.setRequestHeader(header, token);
    // });

    //获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    //发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
        function (data) {
            data = $.parseJSON(data);
            //在提示框中显示返回信息
            $("#hintBody").text(data.msg);
            //显示提示框
            $("#hintModal").modal("show");
            //自动隐藏
            setTimeout(function () {
                $("#hintModal").modal("hide");
                //刷新页面
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 1500);
        }
    );

}
