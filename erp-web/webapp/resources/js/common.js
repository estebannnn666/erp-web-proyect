/**
 * Verifica si se aplica la tecla ENTER
 */
function verifyKeyEnter(e){
	var ev = (e) ? e : event;
	var code = (ev.which) ? ev.which : event.keyCode;
	if(code == 13){
		return true;
	}else{
		return false;
	}
}


function setCursor(inputObject,selectionStart, selectionEnd) {
	if(inputObject.setSelectionRange) {
		inputObject.focus();
		inputObject.setSelectionRange(selectionStart,selectionEnd);
	}else {
		if(inputObject.createTextRange) {
			range=inputObject.createTextRange();
			range.collapse(true);
			range.moveEnd('character',selectionEnd); 
			range.moveStart('character',selectionStart); 
			range.select();
		}
	}
}