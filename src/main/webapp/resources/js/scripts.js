PrimeFaces.locales ['bg'] = {
    monthNames: ['Януари', 'Февруари', 'Март', 'Април', 'Май', 'Юни', 'Юли', 'Август', 'Септември', 'Октомври', 'Ноември', 'Декември'],
    monthNamesShort: ['Яну', 'Фев', 'Мар', 'Апр', 'Май', 'Юни', 'Юли', 'Авг', 'Сеп', 'Окт', 'Ное', 'Дек'],
    dayNames: ['Неделя', 'Понеделник', 'Вторник', 'Сряда', 'Четвъртък', 'Петък', 'Събота'],
    dayNamesShort: ['Нед', 'Пон', 'Вт', 'Ср', 'Чт', 'Пт', 'Съб'],
    dayNamesMin: ['Н', 'П', 'В', 'С ', 'Ч', 'П ', 'С'],
    weekHeader: 'Седмица',
    firstDay: 1,
    isRTL: false,
    showMonthAfterYear: false,
    yearSuffix: '',
    timeText: 'Време',
    hourText: 'Час',
    minuteText: 'Минута',
    secondText: 'Секунда',
    ampm: false,
    month: 'Месец',
    week: 'Седмица',
    day: 'Ден',
    today: 'Днес',
};

function clearEmoticonContent() {
    $(".messages-wrapper .message ss").html("");
}

function changeSelectedBookmarkColor() {
	let colorSquareNew = $(".bookmark-colors-list").find(".bookmark-color.add-style");
	let colorSquareOld = $(".bookmark-colors-list").find(".bookmark-color.selected");
	$(colorSquareNew).addClass("selected").removeClass("add-style");
	$(colorSquareOld).removeClass("selected");
}

function changeColor(color) {
	$("#dropdownColor").css({'background-color': color})
}