package com.darwinsys.calendar;

import java.io.PrintWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * CalendarEvent is a Java 14+ "record" type referring to one event in a calendar.
 * This is not a java.util.Calendar but is application-defined,
 * possibly a List<CalendarEvent>.
 * The list of arguments is long, but there are some factory methods
 * to simplify construction where not all fields are wanted.
 * @param event Description of the event
 * @param eventType An EventType enum for this event
 * @param uuid A UUID, can be had from makeUUID()
 * @param startDate The date or begin date
 * @param startTime The begin time of a partial-day event
 * @param endDate The date or begin date
 * @param endTime The begin time of a partial-day event
 * @param organizerName The name of the event organizer (person or organization)
 * @param organizerEmail The contact email for this event
 * @param location - where the event will take place
 * @param calName - the calendar to which this event belongs
 */
public record CalendarEvent(
	String summary,
	Optional<String> description,
	EventType eventType,
	UUID uuid,
	LocalDate startDate, Optional<LocalTime>startTime,
	Optional<LocalDate> endDate, Optional<LocalTime>endTime,
	Optional<String> organizerName, Optional<String> organizerEmail,
	Optional<String> location,
	Optional<String> calName) {

	static Random r = new Random();
	static int nEvent = r.nextInt() + 42;
	static String defaultCalendar = "work";
	
	public static void setDefaultCalendar(String calName) {
		defaultCalendar = calName;
	}

	/** Present a bit of the event info for debugging */
	public String toString() {
		return "CalendarEvent: " + summary + " starting " + startDate;
	}
	
	final static DateTimeFormatter df = 
			DateTimeFormatter.ofPattern("yyyyMMdd"),
			dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");

	/**
	 * Print the event out in full to a writer, in VCALENDAR VEVENT format.
	 * @param out The open PrintWriter to use
	 * @param wrap True to wrap the event in minimal VCALENDAR infra
	 */
	public void toVCalEvent(PrintWriter out, boolean wrap) {
		if (wrap) {
			putVCalHeader(out);
		}
		out.println("BEGIN:VEVENT");
		out.println("DTSTAMP:" + LocalDate.now());
		out.println("CREATED:" + LocalDate.now());
		out.println("SUMMARY:" + summary);
		description.ifPresent(s->out.println("DESCRIPTION:"+s));
		location.ifPresent(s->out.println("LOCATION:"+s));
		String dtStart = startTime.isPresent() ?
			dtf.format(LocalDateTime.of(startDate, startTime.get())) :
			df.format(startDate);
		out.println("DTSTART;VALUE=DATE:" + dtStart);
		
		LocalDate dtEndDate;
		if (endDate.isPresent())
			dtEndDate = endDate.get();
		else
			dtEndDate = startDate;
		String dtEnd = endTime.isPresent() ?
			dtf.format(LocalDateTime.of(dtEndDate, endTime.get())) :
			df.format(dtEndDate);
		out.println("DTEND;VALUE=DATE:" + dtEnd);		

		out.println("SEQUENCE:" + Math.abs(nEvent++));
		out.printf("UID: %s\n", uuid);
		if (organizerName.isPresent() && organizerEmail.isPresent()) {
			out.printf("ORGANIZER;CN=%s:MAILTO:%s\n", organizerName.get(), organizerEmail.get());
		}
		if (calName.isPresent()) {
			out.println("X-WR-CALNAME;VALUE=TEXT:" + calName.get());
		} else {
			out.println("X-WR-CALNAME;VALUE=TEXT:" + defaultCalendar);
		}
		out.println("END:VEVENT");
		if (wrap) {
			putVCalTrailer(out);
		}
	}

	public static void putVCalTrailer(PrintWriter out) {
		out.println("END:VCALENDAR");
	}

	public static void putVCalHeader(PrintWriter out) {
		out.println("BEGIN:VCALENDAR");
		out.println("CALSCALE:GREGORIAN");
		out.println("X-WR-TIMEZONE;VALUE=TEXT:Canada/Eastern");
		out.println("METHOD:PUBLISH");
		out.println("PRODID:-//Darwin Open Systems//c.d.calendar.CalendarEvent 1.0//EN");
		out.println("VERSION:2.0");
	}

	// STATIC UTILITY METHODS

	public static UUID makeUUID() {
		return UUID.randomUUID();
	}

	// Convenience constructors, for a form of compatibility with previous edition of this class

	/** @return An all-day event for one day only
	 * @param description The text of the event
	 * @param summary Short description
	 * @param location Where the event is
	 * @param year The year
	 * @param month The Month
	 * @param day The day
	 */
	public static CalendarEvent newCalendarEvent(String description, 
			String summary, String location,
			int year, int month, int day) {
		
		// this(EventType.ALLDAY, description, summary, location, year, month, day, 0, 0, 0, 0);
		return new CalendarEvent(
				summary,
				Optional.of(description),
				EventType.ALLDAY,
				CalendarEvent.makeUUID(),
				LocalDate.of(year, month, day), Optional.of(LocalTime.of(0,0)),
				Optional.of(LocalDate.of(year, month, day)), Optional.empty(),
				Optional.empty(), Optional.empty(), // Organizer name, email
				Optional.of(location), 
				Optional.empty()			// Calendar name
				);
	}
	
	/** @return A single-day appointment, having start and end hours
	 * @param description The text of the event
	 * @param summary Short description
	 * @param location Where the event is
	 * @param year The year
	 * @param month The Month
	 * @param day The day
	 * @param startHour the starting time
	 * @param endHour the ending time
	 */
	public static CalendarEvent newCalendarEvent(String description, String summary, String location,
			int year, int month, int day, 
			int startHour, int endHour) {
		
		return new CalendarEvent(
				summary,
				Optional.of(description),
				EventType.APPOINTMENT,
				CalendarEvent.makeUUID(),
				LocalDate.of(year, month, day), Optional.of(LocalTime.of(startHour, 0)),
				Optional.empty(), Optional.of(LocalTime.of(endHour, 0)),
				Optional.empty(), Optional.empty(),
				Optional.empty(),
				Optional.empty());
		
	}
	
	/** @return A single-day appointment, having start and end hours AND minutes
	 * @param description The text of the event
	 * @param summary Short description
	 * @param location Where the event is
	 * @param year The year
	 * @param month The Month
	 * @param day The day
	 * @param startHour the starting time
	 * @param endHour the ending time
	 */
	public static CalendarEvent newCalendarEvent(String description, String summary, String location,
			int year, int month, int day, 
			int startHour, int startMinute, int endHour, int endMinute) {
		
		return new CalendarEvent(
				summary,
				Optional.of(description),
				EventType.APPOINTMENT,
				CalendarEvent.makeUUID(),
				LocalDate.of(year, month, day), Optional.of(LocalTime.of(startHour, startMinute)),
				Optional.empty(), Optional.of(LocalTime.of(endHour, endMinute)),
				Optional.empty(), Optional.empty(),
				Optional.empty(),
				Optional.empty());
		
	}
}
