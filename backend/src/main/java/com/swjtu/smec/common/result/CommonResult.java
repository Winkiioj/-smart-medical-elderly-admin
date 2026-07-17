package com.swjtu.smec.common.result;

/**
 * 统一返回结果封装
 *
 * @param <T> 数据类型
 */
public class CommonResult<T> {

    private int code;
    private String msg;
    private T data;
    private Long total;

    public CommonResult() {}

    public CommonResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(200, "操作成功", data);
    }

    public static <T> CommonResult<T> success(T data, Long total) {
        CommonResult<T> result = new CommonResult<>(200, "操作成功", data);
        result.setTotal(total);
        return result;
    }

    public static <T> CommonResult<T> error(int code, String msg) {
        return new CommonResult<>(code, msg, null);
    }

    // --- getters / setters ---

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
}
