package com.santarest;

/**
 * Created by dirong on 2/2/16.
 */
public class ActionState<A> {

    public enum Status{
        START, FINISH, FAIL
    }

    public A action;
    public Throwable error;
    public Status status;

    public ActionState(A action) {
        this.action = action;
    }

    public ActionState<A> action(A action) {
        this.action = action;
        return this;
    }

    public ActionState<A> error(Throwable error) {
        this.error = error;
        return this;
    }

    public ActionState<A> status(Status status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionState<?> that = (ActionState<?>) o;

        if (action != null ? !action.equals(that.action) : that.action != null) return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ActionState{" +
                "action=" + action +
                ", error=" + error +
                ", status=" + status +
                '}';
    }
}
