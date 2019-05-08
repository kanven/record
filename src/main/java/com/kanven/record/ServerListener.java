package com.kanven.record;

public interface ServerListener {

	public void onNotify(ServerEvent event);

	class ServerEvent {

		private final String id;

		private final EventType type;

		private final Object data;

		public ServerEvent(String id, EventType type, Object data) {
			this.id = id;
			this.type = type;
			this.data = data;
		}

		public String id() {
			return id;
		}

		public EventType type() {
			return type;
		}

		public Object getData() {
			return data;
		}

		public static enum EventType {
			SERVER_STATUS_CHANED, THREAD_POOL_REJECTED;
		}

	}

}
