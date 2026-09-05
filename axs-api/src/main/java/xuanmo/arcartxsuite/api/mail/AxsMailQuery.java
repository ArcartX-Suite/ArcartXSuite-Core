package xuanmo.arcartxsuite.api.mail;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 收件箱查询条件。
 * <p>
 * 传给 {@link AxsMailService#getInbox(java.util.UUID, AxsMailQuery)}。
 * 用于按状态筛选、分页和搜索邮件。
 *
 * @param page                     页码，从 1 开始（最小 1）
 * @param pageSize                 每页条数（最小 1）
 * @param state                    状态筛选，{@code null} 表示不筛选
 * @param search                   搜索关键词（匹配标题/发件人），{@code null} 或空表示不搜索
 * @param hasAvailableAttachments  仅返回含可领取附件的邮件，{@code false} 表示不筛选
 * @since 1.5.0
 */
@ApiStability.Stable
public record AxsMailQuery(
    int page,
    int pageSize,
    @Nullable AxsMailState state,
    @Nullable String search,
    boolean hasAvailableAttachments
) {
    /**
     * 构造一个查全部的默认查询（第 1 页，每页 20 条，不筛选）。
     *
     * @return 默认查询
     */
    public static @NotNull AxsMailQuery defaults() {
        return new AxsMailQuery(1, 20, null, null, false);
    }

    /**
     * 构造一个只查未读的查询。
     *
     * @param pageSize 每页条数
     * @return 未读查询
     */
    public static @NotNull AxsMailQuery unread(int pageSize) {
        return new AxsMailQuery(1, pageSize, AxsMailState.UNREAD, null, false);
    }

    /**
     * 构造一个只含可领取附件的查询。
     *
     * @param pageSize 每页条数
     * @return 可领取附件查询
     */
    public static @NotNull AxsMailQuery claimable(int pageSize) {
        return new AxsMailQuery(1, pageSize, null, null, true);
    }
}
