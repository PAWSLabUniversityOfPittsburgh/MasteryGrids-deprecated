var GridChart = function(_cont, _data, _settings, _txtTitle, _yAxisPos, _doShowXLabels, _colors) {
  
  // =====================================================================================================================
  // == PRIVATE ==========================================================================================================
  // =====================================================================================================================
  
  var cont = _cont;
  
  var settings = _settings;
  
  var scales = { x: null, y: null };
  
  var svg = null;
  var title = null;
  var series = {};
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function construct(data, txtTitle, yAxisPos, doShowXLabels, colors) {
    // Prepare other values and functions:
    var xLblAngle = 45;
    
    settings.calc = {};
    settings.calc.topicOffsetT = svgGetMaxTextBB([txtTitle]).height + 4;
    settings.calc.dimOffsetL   = svgGetMaxTextBB($.map(data.dimensions, function (x) { return x.name; })).width + 10;
    settings.calc.dimMaxW      = svgGetMaxTextBB($.map(data.series, function (x) { return x.topic; })).width + 10;
    settings.calc.dimMaxWCos   = Math.ceil(settings.calc.dimMaxW * Math.cos(xLblAngle * (Math.PI / 180)));
    settings.calc.paddingL     = (doShowYAxis ? settings.padding.l : 10);  // TODO: remove doShowYAxis?
    settings.calc.paddingT     = (doShowXLabels ? settings.calc.dimMaxWCos : 0);
    settings.calc.sqW          = Math.floor((settings.w - settings.calc.paddingL - settings.padding.r - settings.sq.padding) / data.series.length);
    settings.calc.sqH          = Math.floor(settings.calc.sqW * sqProp);
    settings.calc.visW         = ((settings.calc.sqW + settings.sq.padding) * data.series.length) + settings.calc.paddingL + settings.padding.r + settings.calc.dimOffsetL;
    settings.calc.visH         = ((settings.calc.sqH + settings.sq.padding) * data.dimensions.length) + settings.padding.t + settings.padding.b + settings.calc.topicOffsetT + settings.calc.paddingT;
    
    var sepXAggr = 0;
    
    // Prepare scales:
    var scaleX =
      d3.scale.ordinal().
      domain($.map(data.series, function (x) { return x.topic; })).
      rangePoints([ settings.calc.paddingL + settings.calc.sqW / 2 + settings.calc.dimOffsetL, settings.calc.visW - settings.padding.r - settings.calc.sqW / 2 ]);
    
    var scaleY =
      d3.scale.linear().
      domain(settings.scales.y).
      range(colors);
    
    // Prepare axes:
    var axisX =
      d3.svg.axis().
      scale(scaleX).
      orient("bottom").
      tickFormat(function (d,i) { return ("" + d).replace(/_/g, " "); });
    
    // SVG:
    svg =
      d3.select(cont).
      append("svg").
      attr("width", settings.calc.visW + (data.sepX.length * settings.sepX) + settings.calc.dimMaxWCos).
      attr("height", settings.calc.visH);
    
    // Mini bar chart series:
    var mini = { svg: null, settings: miniSettings, series: {} };
    if (miniVis) {
      mini.svg = miniVis($$("td", tr), D, mini.settings, null, 2, false).
        addSeries("pri", { sepX: D.sepX, series: $.map(D.series, function (x) { return 0; }) }, 0, "l-gray", null, null).
        setVis(false).
        style("margin-top", (topicOffsetT + paddingT - mini.settings.padding.t) + "px");
    }
    
    // Series:
    var gSeries = svg.
      append("g").
      attr("class", "grid");
    
    for (var i = 0; i < data.dimensions.length; i++) {
      var dim = data.dimensions[i];
      
      mini.series[dim.id] = [];
      for (var j=0, nj=D.series.length; j < nj; j++) {
        mini.series[dim.id].push(D.series[j].curr[varCls][dim.id]);
      }
      
      sepXAggr = 0;
      gSeries.
        selectAll("grid-" + dim.id).
        data(D.series).
        enter().
        append("rect").
        attr("class", "box").
        attr("x", function (d,i) {
          if ($.inArray(i, D.sepX) !== -1) { sepXAggr += settings.sepX; }
          return (dimOffsetL + paddingL + (settings.sq.padding + i * (sqW + settings.sq.padding)) + sepXAggr);
        }).
        attr("y", ((sqH + settings.sq.padding) * i) + settings.padding.t + topicOffsetT + paddingT).
        attr("width", sqW).
        attr("height", sqH).
        attr("rx", cornerRadius).
        attr("ry", cornerRadius).
        attr("style", function (d) { return "fill: " + scaleY(d.curr[varCls][dim.id]) + ";"; }).
        attr("data-idx", function (d,i) { return i; }).
        attr("data-var-id", dim.id).
        attr("data-var-name", dim.name);
      
      svg.
        append("svg:text").
        attr("class", "dim").
        attr("x", 1).
        attr("y", ((sqH * settings.sq.padding) * i) + (sqH / 2) + 3 + topicOffsetT + paddingT).
        text(dim.name);
    }
  }
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function addSeries(id, data, refVal, cls, fnGetY, fnGetYCls) {
    var sepXAggr = 0;
    refVal = refVal || 0;
    
    var g = svg.
      append("g").
      attr("class", cls);
    
    g.
      selectAll("rect").
      data(data.series).
      enter().
      append("rect").
      attr("class", function (d) { return "bar" + (fnGetYCls ? " " + fnGetYCls(d) : ""); }).
      attr("fill-opacity", 0.5).
      attr("x", function (d,i) {
        if ($.inArray(i, data.sepX) !== -1) { sepXAggr += settings.sepX; }
        return (settings.calc.paddingL + settings.bar.padding + i * (settings.calc.barW + settings.bar.padding) + sepXAggr);
      }).
      attr("y", function (d,i) { return ((fnGetY ? fnGetY(d) : d) < refVal ? scales.y(refVal) : scales.y(fnGetY ? fnGetY(d) : d)); }).
      attr("width", settings.calc.barW).
      attr("height", function (d) { return Math.abs(scales.y(refVal) - scales.y(fnGetY ? fnGetY(d) : d)); });
    
    /*
    if (isInteractive) {
      g.
        selectAll("rect").
        on("mouseover", ehVisBarBarMouseOver).
        on("mouseout", ehVisBarBarMouseOut);
    }
    */
    
    series[id] = { g:g, refVal:refVal, fnGetY:fnGetY, fnGetYCls:fnGetYCls };
    
    return pub;
  }
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function setTitle(txt) {
    title.text(txt || "");
    return pub;
  }
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function setSeriesItemClass(id, cls, indices) {
    var s = series[id];
    
    var i = 0;
    s.g.
      selectAll("rect").
      each(function () {
        if (!indices || $.inArray(i, indices)) d3.select(this).attr("class", cls);
        i++; 
      });
    
    return pub;
  }
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function setVis(v, delay, duration) {
    svg.
      transition().
      delay(delay || 0).
      duration(duration || 0).
      style("opacity", (v ? "1" : "0"));
      //style("display", (v ? "block" : "none"));
    
    return pub;
  }
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function style(s,v) {
    svg.style(s,v);
    return pub;
  }
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function updSeries(id, data, refVal, fnGetY) {
    var sepXAggr = 0;
    var s = series[id];
    
    fnGetY = fnGetY || s.fnGetY;
    refVal = refVal || s.refVal;
    
    s.g.
      selectAll("rect").
      data(data.series).
      transition().
      delay(0).
      duration(100).
      ease("easeInOutQuart").
      attr("x", function (d,i) {
        if ($.inArray(i, data.sepX) !== -1) { sepXAggr += settings.sepX; }
        return (settings.calc.paddingL + settings.bar.padding + i * (settings.calc.barW + settings.bar.padding) + sepXAggr);
      }).
      attr("y", function (d,i) { return ((fnGetY ? fnGetY(d) : d) < refVal ? scales.y(refVal) : scales.y(fnGetY ? fnGetY(d) : d)); }).
      attr("width", settings.calc.barW).
      attr("height", function (d) { return Math.abs(scales.y(refVal) - scales.y(fnGetY ? fnGetY(d) : d)); });
    
    return pub;
  }
  
  
  // =====================================================================================================================
  // == PUBLIC ===========================================================================================================
  // =====================================================================================================================
  
  var pub = {
    
    // -------------------------------------------------------------------------------------------------------------------
    addSeries: function (id, data, refVal, cls, fnGetY, fnGetYCls) { return addSeries(id, data, refVal, cls, fnGetY, fnGetYCls); },
    
    
    // -------------------------------------------------------------------------------------------------------------------
    getSeries: function (id) { return series[id]; },
    
    
    // ---------------------------------------------------------------------------------------------------------------------
    setSeriesItemClass: function (id, cls, indices) { return setSeriesItemClass(id, cls, indices); },
    
    
    // -------------------------------------------------------------------------------------------------------------------
    setTitle: function (txt) { return setTitle(txt); },
    
    
    // -------------------------------------------------------------------------------------------------------------------
    setVis: function (v, delay, duration) { return setVis(v, delay, duration); },
    
    
    // ---------------------------------------------------------------------------------------------------------------------
    style: function (s,v) { return style(s,v); },
    
    
    // -------------------------------------------------------------------------------------------------------------------
    updSeries: function (id, data, refVal, fnGetY) { return updSeries(id, data, refVal, fnGetY); }
  };

  
  // =====================================================================================================================
  // == CONSTRUCT ========================================================================================================
  // =====================================================================================================================
  
  construct(_data, _txtTitle, _yAxisPos, _doShowXLabels, _colors);
  return pub;
};
