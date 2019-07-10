package com.kanven.record.ext.plugins.load.es;

import java.util.Calendar;

enum IndexRule implements Handler {
	YEAR("Y") {

		@Override
		public String handler() {
			Calendar calendar = Calendar.getInstance();
			return "-" + calendar.get(Calendar.YEAR);
		}

		@Override
		public long time() {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MONTH, 12);
			c.set(Calendar.DAY_OF_MONTH, 31);
			setHMSM(c);
			return c.getTime().getTime() - System.currentTimeMillis();
		}

	},
	MONTH("M") {

		@Override
		public String handler() {
			Calendar calendar = Calendar.getInstance();
			return "-" + calendar.get(Calendar.YEAR) + "." + calendar.get(Calendar.MONTH);
		}

		@Override
		public long time() {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
			setHMSM(c);
			return c.getTime().getTime() - System.currentTimeMillis();
		}

	},
	DAY("D") {

		@Override
		public String handler() {
			Calendar calendar = Calendar.getInstance();
			return "-" + calendar.get(Calendar.YEAR) + "." + calendar.get(Calendar.MONTH) + "."
					+ calendar.get(Calendar.DAY_OF_MONTH);
		}

		@Override
		public long time() {
			Calendar c = Calendar.getInstance();
			setHMSM(c);
			return c.getTime().getTime() - System.currentTimeMillis();
		}

	},
	NORMAL("N") {

		@Override
		public String handler() {
			return "";
		}

		@Override
		public long time() {
			return -1;
		}

	};

	private String r;

	private IndexRule(String r) {
		this.r = r;
	}

	public static IndexRule rule(String r) {
		for (IndexRule rule : values()) {
			if (rule.r.equals(r.toUpperCase())) {
				return rule;
			}
		}
		return IndexRule.NORMAL;
	}

	private static void setHMSM(Calendar c) {
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
	}

	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		System.out.println(c.getActualMaximum(Calendar.DAY_OF_MONTH));
	}

}