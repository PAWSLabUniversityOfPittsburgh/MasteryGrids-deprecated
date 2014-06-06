/*
This file has a set of useful functions
*/
var $CONST = {
  align : {
    h: { n:0, c:1, ll:2, lr:3, rl:4, rr:5 },
    v: { n:0, c:1, tt:2, tb:3, bt:4, bb:5 }
  },
  asynchRandParam: "unique_req_id"
};

var $env = {
  isIE: (/MSIE (\d+\.\d+);/.test(navigator.userAgent))
};


// ----------------------------------------------------------------------------------------------------------
function $trim(s) { return (!s ? s : s.replace(/^\s+|\s+$/g, "")); }
function $enc(s)  { return (s === null ? "" : encodeURIComponent("'" + s.replace(/\\/g, "\\\\").replace(/'/g, "\\'") + "'")); }
function $dec(s)  { return decodeURIComponent(s.replace(/\+/g, " ")); };


// ---------------------------------------------------------------------------------------------------------------------
function $join(A, delim) { return $filter(function (x) { return !!x; }, A).join(delim); }


// ----------------------------------------------------------------------------------------------------------
function $_(id) { return document.getElementById(id); }


// ----------------------------------------------------------------------------------------------------------
function $$(type, parent, id, cls, innerHTML) {
  var el = document.createElement(type);
  
  if (id)        el.setAttribute("id", id);
  if (cls)       el.className = cls;
  if (innerHTML) el.innerHTML = innerHTML;
  if (parent)    parent.appendChild(el);
  
  return el;
}


// ---------------------------------------------------------------------------------------------------------------------
/*
 * Because the fucking IE which doesn't support changing some of the attributes after an element 
 * has been added to the DOM, I had to create this otherwise useless method.
 */
function $$input(type, parent, id, cls, value) {
  var el = document.createElement("input");
  el.setAttribute("type", type);
  if (id) el.id = id;
  if (cls) el.className = cls;
  if (value) el.value = value;
  if (parent) parent.appendChild(el);
  return el;
}


// ---------------------------------------------------------------------------------------------------------------------
function $$tbl(parent, id, cls, cellpadding, cellspacing) {
  var tbl = document.createElement("table");
  tbl.setAttribute("cellpadding", cellpadding || "0");
  tbl.setAttribute("cellspacing", cellspacing || "0");
  if (id) tbl.id = id;
  if (cls) tbl.className = cls;
  
  if (parent) parent.appendChild(tbl);
  var tbody = $$("tbody", tbl);
  
  return tbody;
}


// ---------------------------------------------------------------------------------------------------------------------
function $setAttr(o, A) {
  for (var a in A) o.setAttribute(a, A[a]);
  return o;
}


// ---------------------------------------------------------------------------------------------------------------------
function $call(method, url, params, fnCb, doEval, doAlertMsg) {
  if ($env.isIE) {
    url += (url.indexOf("?") === -1 ? "?" : "&") + $CONST.asynchRandParam + "=" + (new Date()).getTime();
  }
  
  var req = new XMLHttpRequest();
  req.open(method, url);
  
  if (fnCb) {
    req.onreadystatechange = function (e) {
      if (req.readyState === 4) {
        if (req.status === 200) {
          if (doEval) {
            var res = null;
            try {
              eval("res = " + req.responseText);
            }
            catch (ex) {
              res = { outcome: false, msg: ex };
            }
            if (!res.outcome && doAlertMsg) alert(res.msg);
            fnCb(res);
          }
          else fnCb(req);
        }
        else {
          var msg = "Server error occurred. Try repeating your last action. If that doesn't work, wait for a while and then try again. I apologize for the inconvenience.";
          if (doAlertMsg) alert(msg);
          fnCb({ outcome: false, msg: msg});
        }
      }
    };
  };
  
  if (params) {
    req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    req.setRequestHeader("Content-length", params.length);
    req.setRequestHeader("Connection", "close");
    req.send(params);
  }
  else req.send(null);
}


// ---------------------------------------------------------------------------------------------------------------------
function $removeChildren(el) {
  if (!el) return;
  while (el.hasChildNodes()) el.removeChild(el.childNodes[0]);
}


// ---------------------------------------------------------------------------------------------------------------------
function $clsAdd(el, x) {
  var cls = el.className;
  
  if (cls.indexOf(x) >= 0 ) return el;
  
  el.className = (cls + " " + x);
  return el;
}


// ---------------------------------------------------------------------------------------------------------------------
function $clsRem(el, x) {
  var cls = el.className;
  
  if (cls.indexOf(x) === -1 ) return el;
  
  var C = cls.replace(/\s+/g, " ").split(/ /);
  for (var i=0,ni=C.length; i < ni; i++) {
    if (C[i] === x) {
      C.splice(i,1);
      i--;
      ni--;
    }
  }
  el.className = C.join(" ");
  
  return el;
}


// ---------------------------------------------------------------------------------------------------------------------
function $show(el) { el.style.display = "block"; }
function $hide(el) { el.style.display = "none";  }


// ---------------------------------------------------------------------------------------------------------------------
function $getHash(delimPair, delimKeyVal) {
  var h = {};
  
  var A = document.location.hash.substr(1).split(delim01);
  for (var i=0, ni=A.length; i < ni; i++) {
    var B = A[i].split(delim02);
    h[B[0]] = B[1];
  }
  
  return h;
}


// ---------------------------------------------------------------------------------------------------------------------
function $getQS() {
  var qs = {};
  
  var A = document.location.search.substr(1).split("&");
  for (var i=0, ni=A.length; i < ni; i++) {
    var B = A[i].split("=");
    qs[B[0]] = B[1];
  }
  
  return qs;
}


// ---------------------------------------------------------------------------------------------------------------------
function $cookieGet(name, delim01, delim02) {
  var tmp = null;
  
  // Retrieve the cookie itself:
  var nameEq = name + "=";
  var C = document.cookie.split(';');
  for (var i = 0; i < C.length; i++) {
    var c = C[i];
    while (c.charAt(0) == ' ') c = c.substring(1, c.length);
    if (c.indexOf(nameEq) == 0) tmp = c.substring(nameEq.length, c.length);
  }
  if (!tmp) return null;
  
  if (!delim01 || !delim02) return tmp;
  
  // Convert the cookie into a hash:
  var c = {};
  
  var A = tmp.split(delim01);
  for (var i=0, ni=A.length; i < ni; i++) {
    var B = A[i].split(delim02);
    c[B[0]] = B[1];
  }
  
  return c;
}


// ---------------------------------------------------------------------------------------------------------------------
function $cookieRem(name) { $cookieSet(name, "", -1); }


// ---------------------------------------------------------------------------------------------------------------------
function $cookieSet(name, value, days) {
  var expires = "";
  if (days) {
    var date = new Date();
    date.setTime(date.getTime() + (days*24*60*60*1000));
    expires = "; expires=" + date.toGMTString();
  }
  document.cookie = name + "=" + value + expires + "; path=/";
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtGet(e) {
  if (typeof e == 'undefined') e = window.event;
  if (typeof e.layerX == 'undefined') e.layerX = e.offsetX;
  if (typeof e.layerY == 'undefined') e.layerY = e.offsetY;
  return e;
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtTgt(e) {
  e = $evtGet(e);
  return e.target || e.srcElement;
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtChar(e) {
  e = $evtGet(e);
  return e.charCode;
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtCode(e) {
  e = $evtGet(e);
  return e.keyCode;
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtKey(e) {
  e = $evtGet(e);
  return (window.event ? event.keyCode : e.which);
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtMouseBtn(e) {
  e = $evtGet(e);
  return e.which || e.button;
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtPrevDef(e) {
  e = e || window.event;
  if (e.preventDefault) e.preventDefault();
  e.returnValue = false;
}


// ---------------------------------------------------------------------------------------------------------------------
function $evtCancelProp(e) {
  if (!e) var e = window.event;
  if (e.stopPropagation) e.stopPropagation();
  e.cancelBubble = true;
}


// ---------------------------------------------------------------------------------------------------------------------
function $lfold(fn, A, init) {
  var res = init;
  for (var i=0, ni=A.length; i < ni; i++) {
    res = fn(res, A[i]);
  }
  return res;
}


// ---------------------------------------------------------------------------------------------------------------------
/*
 * Pass one or more arrays after the 'fn' argument. In the case of multiple arrays they are anticipated to be of the 
 * same size.
 */
function $map(fn) {
  var lstCnt = arguments.length-1;
  var res = [];
  
  if (lstCnt === 1) {  // one extra argument
    if (!(arguments[1] instanceof Array)) {  // and this argument ain't an array
      return fn(arguments[1]);
    }
    
    for (var i=0, ni=arguments[1].length; i < ni; i++) {  // it is an array
      //res[i] = fn(arguments[1][i]);
      res[i] = fn((function (x) { return x; })(arguments[1][i]));
    }
  }
  else {  // multiple extra arguments
    for (var i=0, ni=arguments[1].length; i < ni; i++) {
      var lst = [];
      for (var j=1; j <= lstCnt; j++) {
        lst.push(arguments[j][i]);
      }
      //res[i] = fn(lst);
      res[i] = fn((function (x) { return x; })(lst));
    }
  }
  
  return res;
}


// ---------------------------------------------------------------------------------------------------------------------
function $getCoords(el) {
  var x1=0, y1=0, w=el.offsetWidth, h=el.offsetHeight, x2=el.offsetWidth, y2=el.offsetHeight;
  
  if (!!el.offsetParent) {
    do {
      x1 += el.offsetLeft;
      y1 += el.offsetTop;
    } while (el = el.offsetParent);
  }
  else {
    x1 = el.offsetLeft;
    y1 = el.offsetTop;
  }
  
  return { x1:x1, y1:y1, x2:x1+x2, y2:y1+y2, x0:x1+Math.floor(w/2), y0:y1+Math.floor(h/2) };
}


// ---------------------------------------------------------------------------------------------------------------------
function $getDim(el) { return (el ? { w: el.offsetWidth, h: el.offsetHeight } : null); }


// ---------------------------------------------------------------------------------------------------------------------
/**
 * Centers 'el' horizontally at 'x' and aligns its top with 'y'.
 */
function $setPosCenter(el, isFixed, x, y, doConstrain) {
  el.style.position = (isFixed ? "fixed" : "absolute");
  $show(el);
  
  var dim = $getDim(el);
  
  x = x - (dim.w / 2)
  
  // (1) Account for the window size and scrolling:
  var d = document.documentElement;
  var b = document.body;
  
  var winW = window.innerWidth;
  var winH = window.innerHeight;
  
  if (doConstrain) {
    if (x <= 4) x = 4;
  }
  
  // (3) Set position:
  el.style.left = x + "px";
  el.style.top  = y + "px";
}

