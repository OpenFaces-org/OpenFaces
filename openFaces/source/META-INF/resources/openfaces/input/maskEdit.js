/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2013, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

//TODO: Common: check if user able to specify his own пress etc event from JSF, (addEventListener)
//TODO: browser compatibility issues

O$.MaskEdit = {

  _init:function (inputId, mask, blank, dynamicConstructors, symbolConstructors, dictionary, blankVisible, rolloverClass, focusedClass) {
    var maskEdit = O$.initComponent(inputId,
            {_rolloverClass:rolloverClass,
              _focusedClass:focusedClass
            },
            { _mask:"",
              _blank:blank,
              _dictionary:dictionary,
              _maskInputCursorPosition:0,
              _value:blank,
              _maskValue:[],
              _primaryMaskValue:[],
              _maskSeparatorPosition:[],
              _maskInputPosition:[],
              _dynamicSymbolsPosition:[],
              _isCursorInSeparator:false,
              _numeric:"1234567890",
              _symbol:"`~,.?!@#$%^&*(){}[]",
              _symbolConstructors:[],
              _dynamicConstructors:[],
              _mockInputObject:[]
            }


    );
    maskEdit._oldClick = maskEdit.onClick;
    maskEdit._oldKeyPress = maskEdit.onkeypress;
    maskEdit._oldKeyUp = maskEdit.onkeyup;
    maskEdit._oldKeyDown = maskEdit.onkeydown;
    maskEdit._oldFocus = maskEdit.onfocus;
    maskEdit._oldBlur = maskEdit.onblur;
    maskEdit._oldPaste = maskEdit.onpaste;
    maskEdit._oldInput = maskEdit.oninput;
    if (!blankVisible) {
      if (!maskEdit._oldBlur) {
        O$.addEvent(inputId, "blur", function () {
          if (this.value == this._blank) {
            this.value = "";
          }
        });
      }
      if (!maskEdit._oldFocus) {
        O$.addEvent(inputId, "focus", function () {
          if (this.value == "") {
            this.value = this._blank;
          }
        });
      }
    }

    maskEdit._dictionary = dictionary ? dictionary : "absdefghijklmnopqrstuwwxyz";
    for (var i = 0; i < maskEdit._blank.length; i++) {
      if (maskEdit._blank[i] == mask[i]) {
        maskEdit._maskSeparatorPosition.push(i)
      } else {
        maskEdit._maskInputPosition.push(i);
        maskEdit._mask = maskEdit._mask + mask[i];

      }
      maskEdit._maskValue.push(maskEdit._blank[i]);
      maskEdit._primaryMaskValue.push(maskEdit._blank[i]);
    }

    maskEdit._isFinishPosition = maskEdit._maskInputPosition.length <= 1;
    maskEdit.value = maskEdit._blank;
    maskEdit._cursorPosition = maskEdit._maskInputPosition[0];
    O$._selectTextRange(maskEdit, maskEdit._cursorPosition, maskEdit._cursorPosition);
    for (var i in symbolConstructors) {
      symbolConstructor = [];
      ch = symbolConstructors[i].charAt(1);
      symbolConstructor.push(ch);
      dynamicOccurrenceChar = symbolConstructors[i].substring(3, symbolConstructors[i].length - 1);
      symbolConstructor.push(dynamicOccurrenceChar);
      maskEdit._symbolConstructors.push(symbolConstructor);
    }
    for (var i in dynamicConstructors) {
      dynamicConstructor = [];
      ch = dynamicConstructors[i].charAt(1);
      dynamicConstructor.push(ch);
      ch = dynamicConstructors[i].charAt(3);
      dynamicConstructor.push(ch);
      minandmaxposition = dynamicConstructors[i].substring(5, dynamicConstructors[i].length - 3);
      numberSeparator = minandmaxposition.indexOf(",");
      min = minandmaxposition.substr(0, numberSeparator);
      max = minandmaxposition.substr(numberSeparator + 1, minandmaxposition.length - 1);
      dynamicConstructor.push(min);
      dynamicConstructor.push(max);
      symbolForMaskValue = dynamicConstructors[i].charAt(dynamicConstructors[i].length - 2);
      dynamicConstructor.push(symbolForMaskValue);
      maskEdit._dynamicConstructors.push(dynamicConstructor);
    }

    for (var i in maskEdit._mask) {
      for (var j in maskEdit._dynamicConstructors) {
        if (maskEdit._mask[i] == maskEdit._dynamicConstructors[j][0]) {
          maskEdit._dynamicSymbolsPosition.push(maskEdit._maskInputPosition[i]);
          maskEdit._mockInputObject.push(O$.mockInput._init());
          break;
        }
      }
    }

    O$.extend(maskEdit, {
              onkeypress:function (e) {
                if (this._oldKeyPress) {
                  this._oldKeyPress(e);
                }
                return this._realMaskEditKeyPressListener(e);
              },
              _maskEditKeyPress:function (e) {
                if (this._isFinishPosition)
                  return false;

                e = e || window.event;
                if (e.ctrlKey || e.altKey || e.metaKey)
                  return;
                var pressChar = this._getChar(e || window.event);
                if (!pressChar)
                  return;

                if (this._isValidChar(pressChar)) {
                  if (this._isCursorInSeparator) {
                    this._setCursorPosition(this._cursorPosition + 1, false);
                  }
                  this._maskValue[this._cursorPosition] = pressChar;
                  this.value = this._toStringMaskValue(this._maskValue);

                  if (this._maskInputCursorPosition != (this._maskInputPosition.length - 1)) {
                    this._maskInputCursorPosition++;
                    this._cursorPosition = this._maskInputPosition[this._maskInputCursorPosition];
                    O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                  } else {
                    this._cursorPosition++;
                    this._isFinishPosition = true;
                    O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                  }
                }
                return false;
              },

              _toStringMaskValue:function (maskValue) {
                return maskValue.toString().replace(/\,/g, "");
              },

              _getChar:function (event) {
                if (event.which == null) {
                  if (event.keyCode < 32) return null;
                  return String.fromCharCode(event.keyCode)
                }
                if (event.which != 0 && event.charCode != 0) {
                  if (event.which < 32) return null;
                  return String.fromCharCode(event.which);
                }
                return null;
              },
              _isOccurrenceChar:function (symbol, symbolArray) {
                return symbolArray.indexOf(symbol) != -1;

              },

              isControlKey:function (key) {
                switch (key) {
                  case 8:
                    return this._isKeyBackspace();
                  case 36:
                    return this._isKeyHome();
                  case 35:
                    return this._isKeyEnd();
                  case 37:
                    return this._isKeyLeft();
                  case 39:
                    return this._isKeyRight();
                  case 46:
                    return this._isKeyDelete();
                  case 9:
                    return this._isKeyTab();
                  default:
                    return true;
                }
              },


              onkeydown:function (e) {
                if (this._oldKeyDown) {
                  this._oldKeyDown(e);
                }
                return this._realMaskEditKeyDownListener(e);
              },

              _maskEditKeyDown:function (e) {
                var key = e.keyCode;
                return this.isControlKey(key);
              },

              onkeyup:function (e) {
                if (this._oldKeyUp) {
                  this._oldKeyUp(e);
                }
                return this._realMaskEditKeyUpListener(e);
              },
              _maskEditKeyUp:function (e) {
                return false;
              },

              getSelectionText:function () {
                var txt;
                if (window.getSelection) {
                  txt = window.getSelection().toString();
                } else if (document.getSelection) {
                  txt = document.getSelection();
                } else if (document.selection) {
                  txt = document.selection.createRange().text;
                }
                return txt;
              },

              onclick:function (event) {
                if (this._oldClick) {
                  this._oldClick(event);
                }
                return this._realMaskEditClickListener(event);
              },
              _maskEditOnClick:function (event) {

                event = event || window.event;

                var clickCursorPosition = O$._getCaretPosition(this);
                this._setCursorPosition(clickCursorPosition, true);

              },
              /*   onpaste:function (e) {
               if (this._oldPaste) {
               this._oldPaste(e);
               }
               },
               _maskEditOnPaste:function () {
               if (O$.isExplorer()) {
               e = e || event;
               setTimeout(this._validator(), 0);
               }
               },

               oninput:function (e) {
               if (this._oldInput) {
               this._oldInput(e);
               }
               },
               _maskEditOnInput:function () {
               return this._validator();
               },*/

              _validator:function () {
                var newValue = this._cutNewMask(this.value);
                if (!this._valueMaskValidator(newValue)) this._valueBlankValidator(newValue);
                this.value = this._toStringMaskValue(this._maskValue);
                this._setCursorPosition(this._cursorPosition, false);
                O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                return false;
              },

              _cutNewMask:function (newValue) {
                var endMask = newValue.length - this._blank.length;
                return newValue.substr(this._cursorPosition, endMask);
              },

              _valueMaskValidator:function (mask) {
                var bufMaskCursorPosition = this._cursorPosition;
                if (this._isCursorInSeparator) this._setCursorPosition(this._cursorPosition + 1, false);
                var bufValue = [].concat(this._maskValue);

                for (var i = 0; i < mask.length; i++) {
                  if (this._setCursorPosition(this._cursorPosition + 1, false)) {

                    if (!this._isValidChar(mask.charAt(i))) {
                      this._cursorPosition = bufMaskCursorPosition;
                      return false
                    }
                    if (this._isFinishPosition) {
                      bufValue[this._maskInputPosition[this._maskInputCursorPosition]] = mask[i];
                    } else {
                      bufValue[this._maskInputPosition[this._maskInputCursorPosition - 1]] = mask[i];
                    }
                  } else {
                    this._maskInputCursorPosition = bufMaskCursorPosition;
                    return false
                  }
                }
                this._maskValue = bufValue;
                return true;

              },
              _valueBlankValidator:function (blank) {
                var positionInBlank = 0;
                var mask = "";
                if (blank.length <= this._blank.length - this._cursorPosition) {
                  for (var i = this._cursorPosition; i < blank.length + this._cursorPosition; i++) {
                    var IsMaskSymbol = false;
                    for (var j in this._maskSymbolArray) {
                      if (this._blank[i] == this._maskSymbolArray[j]) {
                        IsMaskSymbol = true;
                      }
                    }
                    if (IsMaskSymbol) {
                      mask = mask.concat(blank[positionInBlank]);
                    } else {
                      if (!(this._blank.charAt(i) == blank[positionInBlank])) return false
                    }
                    positionInBlank++;


                  }
                  return this._valueMaskValidator(mask);
                }
                return false;
              },

              _isValidChar:function (pressChar) {
                var maskChar = this._mask[this._maskInputCursorPosition];
                switch (maskChar) {
                  case "l":
                    return this._isOccurrenceChar(pressChar, this._dictionary + this._dictionary.toUpperCase());
                  case "L":
                    return this._isOccurrenceChar(pressChar, this._dictionary.toUpperCase());
                  case "a":
                    return this._isOccurrenceChar(pressChar, this._dictionary + this._dictionary.toUpperCase() + this._numeric);
                  case "A":
                    return this._isOccurrenceChar(pressChar, this._dictionary.toUpperCase() + this._numeric);
                  case "#":
                    return this._isOccurrenceChar(pressChar, this._numeric);
                  case "~":
                    return this._isOccurrenceChar(pressChar, this._symbol);
                  default:
                    return this._checkForDynamicSymbols(pressChar, maskChar);
                }
              },

              _checkForDynamicSymbols:function (presChar, maskChar) {
                for (var i in this._symbolConstructors) {
                  if (maskChar == this._symbolConstructors[i][0]) {
                    return  this._isOccurrenceChar(presChar, this._symbolConstructors[i][1]);
                  }
                }
                return false;
              }


            }
    )
    ;
    O$.extend(maskEdit, {

              _isKeyRight:function () {
                this._setCursorPosition(this._cursorPosition + 1, false);
                O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);

                return false;
              },
              _isKeyLeft:function () {
                this._setCursorPosition(this._cursorPosition - 1, false);
                O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);

                return false;

              },
              _isKeyEnd:function () {
                this._setCursorPosition(this._maskInputPosition[this._maskInputPosition.length] + 1, false)
                O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
              },
              _isKeyHome:function () {
                this._setCursorPosition(this._maskInputPosition[0], false)
                O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                return false;
              },
              _isKeyDelete:function () {
                if (this._deleteSelectionText()) return false;
                if (this._setCursorPosition(this._cursorPosition + 1, false)) {
                  var oldInputPosition;
                  if (this._isFinishPosition) {
                    oldInputPosition = this._maskInputPosition[this._maskInputCursorPosition];
                  } else {
                    oldInputPosition = this._maskInputPosition[this._maskInputCursorPosition - 1];
                  }
                  this.__deleteAndChangeValue(oldInputPosition);
                  O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                }
                return false;
              },
              _isKeyBackspace:function () {
                if (this._deleteSelectionText()) return false;
                if (this._setCursorPosition(this._cursorPosition - 1, false)) {
                  var oldInputPosition = this._maskInputPosition[this._maskInputCursorPosition];
                  this.__deleteAndChangeValue(oldInputPosition);
                  O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                }
                return false;
              },
              _isKeyTab:function () {
                if (!this._isFinishPosition) {
                  for (var i in this._maskSeparatorPosition) {
                    if (this._cursorPosition < this._maskSeparatorPosition[i]) {
                      var callback = !this._setCursorPosition(this._maskSeparatorPosition[i] + 1, false);
                      O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                      return callback;
                    }
                  }
                }
                return true;
              },
              __deleteAndChangeValue:function (oldInputPosition) {
                this._maskValue[oldInputPosition]
                        = this._primaryMaskValue[oldInputPosition];
                this.value = this._toStringMaskValue(this._maskValue);
              },
              _deleteSelectionText:function () {
                if (this.getSelectionText()) {

                  var clickCursorPosition = O$._getCaretPosition(this);
                  for (var i = 0; i < this.getSelectionText().length; i++) {
                    this._maskValue[clickCursorPosition + i] = this._primaryMaskValue[clickCursorPosition + i];
                  }
                  this.value = this._toStringMaskValue(this._maskValue);
                  O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                  return true;
                }
                return false;
              },
              getValue:function () {
                return this._value
              },
              setValue:function (newValue) {
                return this._valueMaskValidator(newValue) || this._valueBlankValidator(newValue);
              },
              getMask:function () {
                return this._mask
              },
              setMaskAndBlank:function (newMask) {
                this._mask = newMask;
                this._blank = newBlank;
              },

              getBlank:function () {
                return this._blank
              },
              ///////////////////////////////////////////////////////////////////
              //////////////////////////////////////////////////////////////////////
              /////////////////////////////////////////////////////////////
              _setCursorPosition:function (allegedPosition, carecter) {

                if (carecter) {
                  return this.__mouseClickInBlank(allegedPosition);
                }
                else {
                  if (allegedPosition < this._cursorPosition) {
                    return this.__moveCursorInLeft(allegedPosition);
                  } else {
                    return this.__moveCursorInRight(allegedPosition);
                  }
                }
              },
              __mouseClickInBlank:function (allegedPosition) {

                if ((allegedPosition < this._maskInputPosition[0]) ||
                        (allegedPosition > (this._maskInputPosition[this._maskInputPosition.length - 1] + 1))) {
                  O$._selectTextRange(this, this._cursorPosition, this._cursorPosition);
                  event.stopPropagation ? event.stopPropagation() : (event.cancelBubble = true);
                  return;
                }
                this._isFinishPosition = false;
                for (var i in this._maskInputPosition) {
                  if (allegedPosition == this._maskInputPosition[i]) {
                    this._cursorPosition = this._maskInputPosition[i];
                    this._isFinishPosition = false;
                    this._maskInputCursorPosition = i;
                    return;
                  }
                }
                if (allegedPosition == this._maskInputPosition[this._maskInputPosition.length - 1] + 1) {
                  this._isFinishPosition = true;
                  this._cursorPosition = allegedPosition;
                  return;
                }

                this._isCursorInSeparator = true;
                this._cursorPosition = allegedPosition;
              },
              __moveCursorInLeft:function (allegedPosition) {
                if (this._isCursorInSeparator) {

                  for (var i = this._maskInputPosition.length - 2; i >= 0; i--) {
                    if ((this._maskInputPosition[i] < allegedPosition) && (this._maskInputPosition[i + 1] > allegedPosition)) {
                      this._maskInputCursorPosition = i;
                      this._cursorPosition = this._maskInputPosition[i];
                      this._isCursorInSeparator = false;
                      return  true;
                    }
                  }
                }
                if (!(this._maskInputCursorPosition == 0 && this._isFinishPosition == false)) {
                  if ((this._maskInputCursorPosition == 0) && (this._isFinishPosition == true)) {
                    this._maskInputCursorPosition = this._maskInputPosition.length - 1;
                    this._isFinishPosition = false;
                    this._cursorPosition = this._maskInputPosition[this._maskInputCursorPosition];
                    return true;
                  }

                  for (i = this._maskInputPosition.length - 1; i >= 0; i--) {
                    if (this._maskInputPosition[i] == allegedPosition) {
                      this._maskInputCursorPosition = i;
                      this._cursorPosition = allegedPosition;
                      this._isFinishPosition = false;
                      return true;
                    }
                  }
                  this._isFinishPosition = false;
                  this._maskInputCursorPosition--;
                  this._cursorPosition = this._maskInputPosition[this._maskInputCursorPosition];
                  return true;
                } else return false;
              },
              __moveCursorInRight:function (allegedPosition) {
                if (this._isCursorInSeparator) {

                  for (i = 1; i < this._maskInputPosition.length - 1; i++) {
                    if ((this._maskInputPosition[i - 1] < allegedPosition) && (this._maskInputPosition[i] > allegedPosition)) {
                      this._maskInputCursorPosition = i;
                      this._cursorPosition = this._maskInputPosition[i];
                      this._isCursorInSeparator = false;
                      this._isFinishPosition = false;
                      return  true;
                    }
                  }

                  for (i = 1; i < this._maskInputPosition.length; i++) {
                    if (this._maskInputPosition[i] == allegedPosition) {
                      this._maskInputCursorPosition = i;
                      this._cursorPosition = this._maskInputPosition[i];
                      this._isCursorInSeparator = false;
                      this._isFinishPosition = false;
                      return  true;
                    }
                  }

                }
                if (!this._isFinishPosition) {
                  for (i in this._maskInputPosition) {
                    if (this._maskInputPosition[i] == allegedPosition) {
                      this._maskInputCursorPosition = i;
                      this._cursorPosition = allegedPosition;

                      return true;
                    }
                  }
                  if (allegedPosition == this._maskInputPosition[this._maskInputPosition.length - 1] + 1) {
                    this._isFinishPosition = true;
                    this._cursorPosition = allegedPosition;
                    return true;
                  }
                  if (this._maskInputCursorPosition == this._maskInputPosition.length - 1) {
                    this._isFinishPosition = true;
                    this._cursorPosition++;
                    return true;
                  }
                  /*  if (!this._maskInputCursorPosition + 1) {
                   return false;
                   }*/
                  this._maskInputCursorPosition++;
                  this._cursorPosition = this._maskInputPosition[this._maskInputCursorPosition];
                  return true;
                } else return false;
              },

              _returnMaskEditEventListeners:function () {
                maskEdit._realMaskEditKeyPressListener = maskEdit._maskEditKeyPress;
                maskEdit._realMaskEditKeyUpListener = maskEdit._maskEditKeyUp;
                maskEdit._realMaskEditKeyDownListener = maskEdit._maskEditKeyDown;
                maskEdit._realMaskEditClickListener = maskEdit._maskEditOnClick;
                maskEdit._realMaskEditKeyPressListener = maskEdit._maskEditKeyPress;
              }

            }

    );
    maskEdit._returnMaskEditEventListeners();
  }
}
;

