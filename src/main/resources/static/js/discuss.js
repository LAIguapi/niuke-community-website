$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);

});

function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : "赞");

            } else {
                alert(data.msg);
            }
        }
    );
}

//置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
                alert("置顶成功");
            } else {
                alert(data.msg);
            }
        }
    )
}

//加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
                alert("加精成功");
            } else {
                alert(data.msg);
            }
        }
    )
}

//删除
function setDelete() {
    if (confirm("请确定是否删除")){
        $.post(
            CONTEXT_PATH + "/discuss/delete",
            {"id": $("#postId").val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    //跳转到主页
                    location.href = CONTEXT_PATH + "/index";
                } else {
                    alert(data.msg);
                }
            }
        )
    }else {
        alert("删除操作取消");
    }
}

