var BarChart = function(_cont, _gridData, _settings, _txtTitle, _yAxisPos, _doShowXLabels) {
  
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
  function construct(gridData, txtTitle, yAxisPos, doShowXLabels) {
    // Prepare other values and functions:
    settings.calc = {};
    settings.calc.paddingL = (yAxisPos === 1 ? settings.padding.l : 1);
    settings.calc.barW     = Math.floor((settings.w - settings.calc.paddingL - settings.padding.r - settings.bar.padding) / gridData.series[0].data.length);
    settings.calc.visW     = ((settings.calc.barW + settings.bar.padding) * gridData.series[0].data.length) + settings.calc.paddingL + settings.padding.r;
    settings.calc.getBarH  = function (y) { return settings.h - settings.padding.b - scaleY(y); };
    
    var sepXAggr = 0;
    
    // Prepare scales:
    scales.x =
      d3.scale.ordinal().
      domain(gridData.topics).
      rangePoints([ settings.calc.paddingL + settings.calc.barW / 2, settings.calc.visW - settings.padding.r - settings.calc.barW / 2 ]);
    
    scales.y =
      d3.scale.linear().
      domain(settings.scales.y).
      range([ settings.h - settings.padding.b, settings.padding.t ]);
    
    // Prepare axes:
    var axisX =
      d3.svg.axis().
      scale(scales.x).
      orient("bottom").
      tickFormat(function (d,i) { return ("" + d).replace(/_/g, " "); });
    
    var axisY =
      d3.svg.axis().
      scale(scales.y).
      orient((yAxisPos === 1 ? "left" : "right")).
      ticks(settings.axes.y.ticks).
      tickValues(settings.axes.y.tickValues).
      tickFormat(d3.format("%")).
      tickSize(0).
      tickSubdivide(true);
    
    // SVG:
    svg =
      d3.select(cont).
      append("svg").
      attr("width", settings.calc.visW + (gridData.sepX.length * settings.sepX)).
      attr("height", settings.h);
    
    // Reference lines:
    svg.
      append("g").
      attr("class", "ref-line").
      selectAll("ref-line").
      data(settings.axes.y.refLines).
      enter().
      append("line").
      attr("x1", settings.calc.paddingL).
      attr("x2", settings.calc.visW + (gridData.sepX.length * settings.sepX) - settings.padding.r).
      attr("y1", function (d) { return scales.y(d); }).
      attr("y2", function (d) { return scales.y(d); });
    
    // X axis:
    if (doShowXLabels) {
      sepXAggr = 0;
      svg.
        append("g").
        attr("class", "x-axis").
        call(axisX).
        selectAll("text").
        style("text-anchor", "start").
        attr("dx", "-" + (settings.h - settings.padding.t - settings.padding.b) + "px").
        attr("dy", function (d,i) {
          if ($.inArray(i, gridData.sepX) !== -1) { sepXAggr += settings.sepX; }
          return (-5 + sepXAggr) + "px";
        }).
        attr("transform", function (d) { return "rotate(-90)"; });
    }
    
    // Title:
    title = svg.append("text").
      attr("class", "title").
      attr("dx", (settings.calc.paddingL + 6) + "px").
      attr("dy", (settings.padding.t + 14) + "px").
      text(txtTitle || "");
    
    // Y axis:
    if (yAxisPos !== 0) {
      var xAxisTrX = (yAxisPos === 1 ? settings.calc.paddingL : settings.calc.visW + (gridData.sepX.length * settings.sepX) - settings.padding.r);
      svg.
        append("g").
        attr("class", "y-axis").
        attr("transform", "translate(" + xAxisTrX + ", 0)").
        call(axisY);
    }
    
    // Border:
    svg.
      append("rect").
      attr("class", "border").
      attr("x", settings.calc.paddingL).
      attr("y", settings.padding.t).
      attr("width", settings.calc.visW - settings.calc.paddingL - settings.padding.r + (gridData.sepX.length * settings.sepX)).
      attr("height", settings.h - settings.padding.t - settings.padding.b);
  }
  
  
  // ---------------------------------------------------------------------------------------------------------------------
  function addSeries(id, gridData, seriesIdx, refVal, cls, fnGetY, fnGetYCls) {
    var sepXAggr = 0;
    refVal = refVal || 0;
    
    var g = svg.
      append("g").
      attr("class", cls);
    
    g.
      selectAll("rect").
      data(gridData.series[seriesIdx].data).
      enter().
      append("rect").
      attr("class", function (d) { return "bar" + (fnGetYCls ? " " + fnGetYCls(d) : ""); }).
      attr("fill-opacity", 0.5).
      attr("x", function (d,i) {
        if ($.inArray(i, gridData.sepX) !== -1) { sepXAggr += settings.sepX; }
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
  function updSeries(id, gridData, seriesIdx, refVal, fnGetY) {
    var sepXAggr = 0;
    var s = series[id];
    
    fnGetY = fnGetY || s.fnGetY;
    refVal = refVal || s.refVal;
    
    s.g.
      selectAll("rect").
      data(gridData.series[seriesIdx].data).
      transition().
      delay(0).
      duration(100).
      ease("easeInOutQuart").
      attr("x", function (d,i) {
        if ($.inArray(i, gridData.sepX) !== -1) { sepXAggr += settings.sepX; }
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
    addSeries: function (id, gridData, seriesIdx, refVal, cls, fnGetY, fnGetYCls) { return addSeries(id, gridData, seriesIdx, refVal, cls, fnGetY, fnGetYCls); },
    
    
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
    updSeries: function (id, gridData, seriesIdx, refVal, fnGetY) { return updSeries(id, gridData, seriesIdx, refVal, fnGetY); }
  };

  
  // =====================================================================================================================
  // == CONSTRUCT ========================================================================================================
  // =====================================================================================================================
  
  construct(_gridData, _txtTitle, _yAxisPos, _doShowXLabels);
  return pub;
};
