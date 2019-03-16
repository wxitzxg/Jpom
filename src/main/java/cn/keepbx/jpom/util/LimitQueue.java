package cn.keepbx.jpom.util;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author jiangzeyin
 * @date 2019/3/16
 */
public class LimitQueue extends ConcurrentLinkedDeque<String> {
    private final int limit;

    public LimitQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean offerLast(String s) {
        pollOver();
        return super.offerLast(s);
    }

    private void pollOver() {
        if (size() > limit) {
            poll();
        }
    }
}