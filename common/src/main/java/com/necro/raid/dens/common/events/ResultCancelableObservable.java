package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import com.cobblemon.mod.common.api.reactive.CancelableObservable;

public class ResultCancelableObservable<T extends Cancelable> extends CancelableObservable<T> {
    public boolean postWithResult(T event) {
        this.emit(event);
        return !event.isCanceled();
    }
}
