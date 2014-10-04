/**
 * TODO
 *   Widget
 *     Utilize overlays to display activities (like to be implemented in the main interface)
 *     Grant immediate (i.e., in the same window) access to activities
 *       There will not be enough room to show activities but Peter wants it that way
 *     Ask server what to provide back
 *   
 *   Timeline
 *     Make the name row hideable
 *   
 *   Adding overlay
 *     Make topic clickable and they go to the activities grids [done]
 *     Topic grids
 *       Click on questions cell shows only questions overlay [done]
 *       Click on average cell shows all activities overlay [done]
 *     Do not show cells for topics with no activities [todo]
 *     On questions grid (i.e., resource-focus mode)
 *       Show overlays as a grid with the color scheme of the row clicked (me, me vs grp, grp) [done]
 *     Overlay itself
 *       Same grid as the topic one but smaller cells if possible (names of activites on the top) [done]
 *     TODO
 *       Redraw the overlay upon closing the activity window
 *       Hide the overlay when the user "clicks away"
 *       Add a small bar chart for activities (that would require new design because currently there is only one bar chart SVG to which every grid refers to)
 *       Highlight the entire column for which activities are being shown to make it clear to users what they are looking at
 *         Cell shadow could be good and natural here since the overlay has a shadow as well
 *       Experiment with using color to bind the overlay to the activity window (currently, orange #faa200 is used in that window, but only when recommended activities are shown, i.e., after a studnet's fuck up)
 *   Sort others by the resource upon selecting it
 *   
 *   Make students choice of if they are anonymous
 *   Add help
 *   Bar chart is too far to the right for grids with column names. See if this can easily be changed.
 * 
 * URLs
 *    1 : http://adapt2.sis.pitt.edu/um-vis-dev/index.html?usr=yam14&grp=IS172013Fall&sid=test&cid=1&ui-tbar-mode-vis=0&ui-tbar-rep-lvl-vis=0&ui-tbar-topic-size-vis=0
 *   16 : http://adapt2.sis.pitt.edu/um-vis-dev/index.html?usr=lmo17&grp=IS172013Fall&sid=test&cid=1&ui-tbar-mode-vis=0&ui-tbar-rep-lvl-vis=0&ui-tbar-topic-size-vis=0
 */


var CONST = {
  appName    : "MasteryGrids",
  cookies    : { days: 355 },
  defTopN    : 10,  // the default 'n' in the "Top n" group
  log        : { sep01: ",", sep02: ":" },  // separators used for logging
  msg        : {
    actLoadRec_notFound: "Due to an error the activity you have selected is not available at this time despite being on the recommended list. Please select a different activity."
  },
  scrollTime : 500,  // after how much time log scrolling position [ms]
  vis        : {
    barAbsL          : { w:600, h:160, padding: { l:35, r: 1, t:4, b: 4 }, bar:    {        padding:1 },          sepX: 20, scales: { y: [0,1]    }, axes: { y: { ticks:3, tickValues: [0.00, 0.50, 1.00],               refLines: [0.25, 0.50, 0.75] } } },
    barAbsS          : { w:600, h: 40, padding: { l:35, r: 1, t:1, b: 1 }, bar:    {        padding:1 },          sepX: 20, scales: { y: [0,1]    }, axes: { y: { ticks:2, tickValues: []                ,               refLines: []                 } } },
    barDevL          : { w:600, h:160, padding: { l:35, r: 1, t:4, b: 4 }, bar:    {        padding:1 },          sepX: 20, scales: { y: [-1,1]   }, axes: { y: { ticks:5, tickValues: [-1.00, -0.50, 0.00, 0.50, 1.00], refLines: [-0.50, 0.50]      } } },
    
    barAbsMini       : { w:300, h:100, padding: { l: 1, r:35, t:4, b: 4 }, bar:    {        padding:1 },          sepX: 10, scales: { y: [0,1]    }, axes: { y: { ticks:3, tickValues: [0.00, 0.50, 1.00],               refLines: [0.25, 0.50, 0.75] } } },
    barDevMini       : { w:300, h:100, padding: { l: 1, r:35, t:4, b: 4 }, bar:    {        padding:1 },          sepX: 10, scales: { y: [-1,1]   }, axes: { y: { ticks:5, tickValues: [-1.00, -0.50, 0.00, 0.50, 1.00], refLines: [-0.50, 0.50]      } } },
    
    bubbleAbsL       : { w:600, h:160, padding: { l:35, r: 1, t:4, b: 4 }, bubble: {        padding:6, rMax:10 }, sepX: 20, scales: { y: [0,1]    }, axes: { y: { ticks:3, tickValues: [0.00, 0.50, 1.00],               refLines: [0.25, 0.50, 0.75] } } },
    bubbleAbsS       : { w:600, h: 40, padding: { l:35, r: 1, t:1, b: 1 }, bubble: {        padding:6, rMax:10 }, sepX: 20, scales: { y: [0,1]    }, axes: { y: { ticks:2, tickValues: []                ,               refLines: []                 } } },
    bubbleDevL       : { w:600, h:160, padding: { l:35, r: 1, t:4, b: 4 }, bubble: {        padding:6, rMax:10 }, sepX: 20, scales: { y: [-1,0,1] }, axes: { y: { ticks:5, tickValues: [-1.00, -0.50, 0.00, 0.50, 1.00], refLines: [-0.50, 0.50]      } } },
    
    gridAbs          : { w:600,        padding: { l:35, r:10, t:1, b:10 }, sq:     { w: 30, padding:1 },          sepX: 15, scales: { y: [0.0, 0.2, 0.4, 0.5, 0.6, 0.8, 1.0]                                     } },
    gridDev          : { w:600,        padding: { l:35, r:10, t:1, b:10 }, sq:     { w: 30, padding:1 },          sepX: 15, scales: { y: [-1.0, -0.8, -0.6, -0.5, -0.4, -0.2, 0.0, 0.2, 0.4, 0.5, 0.6, 0.8, 1.0] } },
    
    gridAbsAct       : { w:600,        padding: { l:35, r:10, t:1, b:10 }, sq:     { w: 26, padding:1 },          sepX: 15, scales: { y: [0.0, 0.2, 0.4, 0.5, 0.6, 0.8, 1.0]                                     } },
    gridDevAct       : { w:600,        padding: { l:35, r:10, t:1, b:10 }, sq:     { w: 26, padding:1 },          sepX: 15, scales: { y: [-1.0, -0.8, -0.6, -0.5, -0.4, -0.2, 0.0, 0.2, 0.4, 0.5, 0.6, 0.8, 1.0] } },
    
    actWindow        : { w:800, h:420},
    
    otherIndCellH    : { def: 12, min: 2, max: 20 },  // [px]
    minCellSizeRatio : 0.25,
    mode             : { grp: 0, ind: 1 },
    seqStars         : true,
    colors             : {
        //me               : colorbrewer.PuRd,
        me               : colorbrewer.Greens,
        grp              : colorbrewer.Blues,
        //grp              : colorbrewer.OrRd,
//        rev              : [],
//        grpRev           : [],
//        spectralRev      : [],
        spectral         : colorbrewer.Spectral,
        indiv            : colorbrewer.Greys,
        sequencing       : colorbrewer.OrRd[6][5]
        
    }
  },
  comparison         : { grpActive : true, meGrpActive : true, othersActive : true},

  uriServer  : "http://adapt2.sis.pitt.edu/aggregate/"
  //uriServer  : "http://localhost:8080/aggregate_git/"
};

var qs = {};  // query string parsed into key-value pairs

var state = {
  args   : {},  // set in the loadData_cb() function
  curr   : { usr: "", grp: "", sid: "", cid: "" },
  vis : {
    act              : {
      act        : null,
      resId      : null,
      actIdx     : -1,
      rsp        : { result: -1, rec: null, fb: null },  // server's response to the activity outcome
      recIdx     : -1,  // the index of the currently selected recommended activity (in the 'state.vis.act.rsp.recomm' array)
      doUpdState : false,
    },
    grid             : {
      cellIdxMax   : 0,
      cellIdxSel   : -1,
      cellSel      : null,
      cornerRadius : 4,
      name         : null,  // the name of the last clicked grid
      xLblAngle    : 45
    },
    isDefBubbleClip  : false,
    isMouseBtn1      : false,
    mode             : CONST.vis.mode.grp,
    otherIndCellH    : 12,  // [px]
    resIdx           : -2,  // there are two entries in the combo box before the first actual resource
    topicIdx         : -1,  // selected topic index
    topicSize        : {
      idx  : 0,
      attr : ""
    },
    lastCellSel      : {
        cellIdxSel   : -1,
        cellSel      : null,
        topicIdx     : -1,
        gridName     : null,
        doMe         : false,
        doVs         : false,
        doGrp        : false
    }
  }
};

var ui = {
  vis : {
    act         : { cont: null, title: null, frame: null, frameRec: null, recLst: null, recLstSel: null, fbDiffCont: null, fbDiffTxt: null, fbDiffBtns: [null, null, null], fbRecCont: null, fbRecTxt: null, fbRecBtns: [null, null, null] },
    grid        : {
      cont   : { me: null, grp: null, others: null },
      me     : { tbar: { sortBy: null, sortDir: null, sortByIdx: 0, sortDirIdx: 0 } },
      grp    : { tbar: {} },
      others : { tbar: { sortBy: null, sortDir: null, sortByIdx: 0, sortDirIdx: 0 } }
    },
    scrollTimer : null,
    svgCommon   : null,
    sunburst    : null,
    actLst      : { cont: null, topicCellX: [] },
    helpDlg     : { title: null, cont: null }
  }
};

var othersTitle = "Students in the class";

var data = null;  // the data requested from the server



// ------------------------------------------------------------------------------------------------------
/**
 * This is the object which should cummulate functions which can be called from other Web apps and Web
 * pages like activities launched either in separate windows or iframes.
 */
var vis = {
  actDone: function (res) {
    var uri = CONST.uriServer + "GetContentLevels?usr=" + state.curr.usr + "&grp=" + state.curr.grp + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid + "&mod=user&sid=" + state.curr.sid + "&lastActivityId=" + state.vis.act.act.id + "&res=" + res;
    $call("GET", uri, null, actDone_cb, true, false);
  },
  
  actUpdState: function (isImmediate) {
    if (isImmediate) {
      // TODO
    }
    else state.vis.act.doUpdState = true;
  },
  
  actLoad: function () {
    $hide(ui.vis.act.fbDiffCont);
    $hide(ui.vis.act.fbRecCont);
    ui.vis.act.fbRecTxt.innerHTML = "";
    
    log(
      "action"               + CONST.log.sep02 + "activity-reload"   + CONST.log.sep01 +
      "activity-topic-id"    + CONST.log.sep02 + getTopic().id       + CONST.log.sep01 +
      "activity-resource-id" + CONST.log.sep02 + state.vis.act.resId + CONST.log.sep01 +
      "activity-id"          + CONST.log.sep02 + getAct().id,
      true
    );
  },
  
  actSubmit: function () {
    $clsAdd(document.body, "loading");
  },
  
  loadingHide: function () {
    $clsRem(document.body, "loading");
  },
  
  loadingShow: function () {
    $clsAdd(document.body, "loading");
  }
};


// ------------------------------------------------------------------------------------------------------
/**
 * Closes an activity which has been opened before.
 */
function actClose() {
  log(
    "action"               + CONST.log.sep02 + "activity-close"    + CONST.log.sep01 +
    "activity-topic-id"    + CONST.log.sep02 + getTopic().id       + CONST.log.sep01 +
    "activity-resource-id" + CONST.log.sep02 + state.vis.act.resId + CONST.log.sep01 +
    "activity-id"          + CONST.log.sep02 + getAct().id,
    true
  );
  
  if (state.vis.act.actIdx === -1) return;
  
  ui.vis.act.frame.src = "empty.html";
  
  // (1) Hide the window:
  $hide(ui.vis.act.cont);
  
  $hide(ui.vis.act.recLst);
  $hide(ui.vis.act.fbRecCont);
  $hide(ui.vis.act.frameRec);
  $show(ui.vis.act.frame);
  
  if (ui.vis.act.recLstSel !== null) $clsRem(ui.vis.act.recLstSel, "sel");
  ui.vis.act.recLstSel = null;
  
  ui.vis.act.frame    .src = "empty.html";
  ui.vis.act.frameRec .src = "empty.html";
  
  // (2) Deselect the activity's grid cell:
  var box = state.vis.grid.cellSel.select(".grid-cell-inner").select(".box");
  box.
    transition().delay(0).duration(100).ease("easeInOutQuart").
    attr("rx", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
    attr("ry", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
    attr("filter", "").
    style("stroke", "");
  
  state.vis.grid.cellIdxSel = -1;
  state.vis.grid.cellSel    = null;
  
  // (3) Update the activity grids:
  var res = getRes(state.vis.act.resId);
  if (res.updateStateOn && (res.updateStateOn.winClose || (res.updateStateOn.winCloseIfAct && state.vis.act.doUpdState))) {
    vis.loadingShow();
    
    actUpdGrids(true, function () { vis.loadingHide(); });
  }
  else if (state.vis.act.recIdx >= 0) {
    var res = getRes(state.vis.act.rsp.rec[state.vis.act.recIdx].resourceId);
    if (res.updateStateOn && (res.updateStateOn.winClose || (res.updateStateOn.winCloseIfAct && state.vis.act.doUpdState))) {
      vis.loadingShow();
      actUpdGrids(true, function () { vis.loadingHide(); });
    }
  }
  
  // (4) Other:
  state.vis.act.act        = null;
  state.vis.act.resId      = null;
  state.vis.act.actIdx     = -1;
  state.vis.act.recIdx     = -1;
  state.vis.act.doUpdState = false;
}


// ------------------------------------------------------------------------------------------------------
/**
 * TODO: Optimize by only updading the "Me" and "Me versus group" grids.  At this point these grids are 
 * being redrawn which is no big deal since no other grids are redrawn.  Consequently, this todo is of 
 * small priority as it wouldn't improve the performance by much (not even sure if it'd be noticeable).
 */
function actDone_cb(rsp) {
  state.vis.act.rsp.result = rsp.lastActivityRes;
  state.vis.act.rsp.rec    = rsp.recommendation;
  state.vis.act.rsp.fb     = rsp.feedback;
  
  log(
    "action"               + CONST.log.sep02 + "activity-done"          + CONST.log.sep01 +
    "activity-topic-id"    + CONST.log.sep02 + getTopic().id            + CONST.log.sep01 +
    "activity-resource-id" + CONST.log.sep02 + state.vis.act.resId      + CONST.log.sep01 +
    "activity-id"          + CONST.log.sep02 + getAct().id              + CONST.log.sep01 +
    "activity-result"      + CONST.log.sep02 + state.vis.act.rsp.result,
    true
  );
  
  // (1) Update the learner:
  data.learners[getMe(true)] = rsp.learner;
  
  var me = getMe(false);
  visAugmentData_addAvgTopic ([me]);
  visAugmentData_addAvgRes   ([me]);
  
  // (2) Recommended activities:
  // (2.1) Remove the previous recommendations:
  while (ui.vis.act.recLst.children.length > 2) ui.vis.act.recLst.removeChild(ui.vis.act.recLst.children[2]);
  
  // (2.2) At least one activity has been recommended:
  if (rsp.recommendation && rsp.recommendation.length > 0) {
    
    $show(ui.vis.act.recLst);
    
    $clsAdd(ui.vis.act.recLst.children[0], "sel");
    ui.vis.act.recLstSel = ui.vis.act.recLst.children[0];
    
    for (var i=0, ni=rsp.recommendation.length; i < ni; i++) {
      var rec = rsp.recommendation[i];
      
      var topic = null;
      for (var j=0, nj=data.topics.length; j < nj; j++) { if (data.topics[j].id === rec.topicId) topic = function (j) { return data.topics[j]; }(j); }
      if (topic === null) continue;
      
      var act = null;
      for (var j=0, nj=topic.activities[rec.resourceId].length; j < nj; j++) { if (topic.activities[rec.resourceId][j].id === rec.activityId) act = function (j) { return topic.activities[rec.resourceId][j]; }(j); }
      if (act === null) continue;
      
      var div = $$("div", ui.vis.act.recLst);
      var scaleMe =  // TODO: Make this scale thing more general.
        d3.scale.linear().
        domain(CONST.vis.gridAbs.scales.y).
        range(["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]));
      $$("span", div, null, "grid-cell", "&nbsp;&nbsp;&nbsp;&nbsp;").style.backgroundColor = scaleMe(getMe().state.activities[rec.topicId][rec.resourceId][rec.activityId].values[getRepLvl().id]);
      $$("span", div, null, null, "2." + (i+1) + ". " + act.name);
      div.onclick = function (i) {
        return function (e) {
          
          if (ui.vis.act.recLstSel !== null) $clsRem(ui.vis.act.recLstSel, "sel");
          
          var div = $evtTgt(e);
          if (div.nodeName.toLowerCase() !== "div") div = div.parentNode;  // in case a nested span element has been clicked
          $clsAdd(div, "sel");
          ui.vis.act.recLstSel = div;
          
          actLoadRec(i);
        };
      }(i);
    }
  }
  
  // (2.3) Nothing has been recommended:
  else {
    $hide(ui.vis.act.recLst);
    $hide(ui.vis.act.fbRecCont);
  }
  
  // (3) Activity feedback:
  if (state.vis.act.rsp.result === 1 && state.vis.act.rsp.fb && state.vis.act.rsp.fb.id) {
    $show(ui.vis.act.fbDiffCont);
    ui.vis.act.fbDiffBtns[0].prop("checked", false).button("refresh");
    ui.vis.act.fbDiffBtns[1].prop("checked", false).button("refresh");
    ui.vis.act.fbDiffBtns[2].prop("checked", false).button("refresh");
  }
  else {
    $hide(ui.vis.act.fbDiffCont);
  }
  
  // (4) Update the activity grids:
  var res = getRes(state.vis.act.resId);
  if (res.updateStateOn && res.updateStateOn.done) {
      actUpdGrids(false, null);
  }
  
  // (5) Other:
  vis.loadingHide();
}


// ------------------------------------------------------------------------------------------------------
function actFbDiff(val) {
  var uri = CONST.uriServer + "StoreFeedback?usr=" + state.curr.usr + "&grp=" + state.curr.grp + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid + "&srcActivityId=" + state.vis.act.act.id + "&srcActivityRes=1&feedbackId=" + state.vis.act.rsp.fb.id + "&feedbackItemsIds=ques_difficulty&responses=" + val + "&recommendationId=";
  $call("GET", uri, null, null, true, false);
  
  log(
    "action"                  + CONST.log.sep02 + "activity-feedback-set-difficulty" + CONST.log.sep01 +
    "activity-topic-id"       + CONST.log.sep02 + getTopic().id                      + CONST.log.sep01 +
    "activity-resource-id"    + CONST.log.sep02 + state.vis.act.resId                + CONST.log.sep01 +
    "activity-id"             + CONST.log.sep02 + getAct().id                        + CONST.log.sep01 +
    "feedback-id"             + CONST.log.sep02 + state.vis.act.rsp.fb.id            + CONST.log.sep01 +
    "feedback"                + CONST.log.sep02 + val,
    true
  );
}


// ------------------------------------------------------------------------------------------------------
function actFbRec(val) {
  var rec = getRec();
  if (!rec._rt) rec._rt = {};
  rec._rt.fb = val;
  
  var uri = CONST.uriServer + "StoreFeedback?usr=" + state.curr.usr + "&grp=" + state.curr.grp + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid + "&srcActivityId=" + state.vis.act.act.id + "&srcActivityRes=" + state.vis.act.rsp.result + "&feedbackId=&feedbackItemsIds=&responses=" + val + "&recommendationId=" + getRec().recommendationId;
  $call("GET", uri, null, null, true, false);
  
  log(
    "action"                           + CONST.log.sep02 + "activity-recommended-feedback-set" + CONST.log.sep01 +
    "activity-original-topic-id"       + CONST.log.sep02 + getTopic().id                       + CONST.log.sep01 +
    "activity-original-resource-id"    + CONST.log.sep02 + state.vis.act.resId                 + CONST.log.sep01 +
    "activity-original-id"             + CONST.log.sep02 + getAct().id                         + CONST.log.sep01 +
    "activity-recommended-topic-id"    + CONST.log.sep02 + rec.topicId                         + CONST.log.sep01 +
    "activity-recommended-resource-id" + CONST.log.sep02 + rec.resourceId                      + CONST.log.sep01 +
    "activity-recommended-id"          + CONST.log.sep02 + rec.activityId                      + CONST.log.sep01 +
    "recommendation-id"                + CONST.log.sep02 + rec.recommendationId                + CONST.log.sep01 +
    "recommendation-rank"              + CONST.log.sep02 + rec.rank                            + CONST.log.sep01 +
    "recommendation-score"             + CONST.log.sep02 + rec.score                           + CONST.log.sep01 +
    "feedback"                         + CONST.log.sep02 + val,
    true
  );
}


// ------------------------------------------------------------------------------------------------------
/**
 * Loads one of the recommended activities.
 */
function actLoadRec(idx) {
  if (state.vis.act.recIdx === idx) return;
  
  // (1) Update the activity grids:
  if (state.vis.act.recIdx >= 0) {
    var res = getRes(state.vis.act.rsp.rec[state.vis.act.recIdx].resourceId);
    if (res.updateStateOn && (res.updateStateOn.winClose || (res.updateStateOn.winCloseIfAct && state.vis.act.doUpdState))) {
      vis.loadingShow();
      actUpdGrids(true, function () { vis.loadingHide(); });
    }
  }
  
  // (2) Identify topic and acticity:
  state.vis.act.recIdx = idx;
  
  var rec = getRec();
  
  var topic = null;
  for (var j=0, nj=data.topics.length; j < nj; j++) { if (data.topics[j].id === rec.topicId) topic = function (j) { return data.topics[j]; }(j); }
  if (topic === null) return alert(CONST.msg.actLoadRec_notFound);
  
  var act = null;
  for (var j=0, nj=topic.activities[rec.resourceId].length; j < nj; j++) { if (topic.activities[rec.resourceId][j].id === rec.activityId) act = function (j) { return topic.activities[rec.resourceId][j]; }(j); }
  if (act === null) return alert(CONST.msg.actLoadRec_notFound);
  
  // (3) Mange frames:
  $hide(ui.vis.act.frame);
  $show(ui.vis.act.frameRec);
  
  ui.vis.act.frameRec.src = act.url + "&grp=" + state.curr.grp + "&usr=" + state.curr.usr + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid;
  
  // (4) Manage feedback:
  if (rec.feedback && rec.feedback.text && rec.feedback.text.length > 0) {
    var actName = getActRec().name;
    ui.vis.act.fbRecTxt.innerHTML = rec.feedback.text.replace(actName, "2." + (idx+1) + ". " + actName);
    $show(ui.vis.act.fbRecCont);
  }
  else {
    $hide(ui.vis.act.fbRecCont);
    ui.vis.act.fbRecTxt.innerHTML = "";
  }
  
  ui.vis.act.fbRecBtns[0].prop("checked", (!rec._rt || rec._rt.fb !== 0 ? false : true)).button("refresh");
  ui.vis.act.fbRecBtns[1].prop("checked", (!rec._rt || rec._rt.fb !== 1 ? false : true)).button("refresh");
  ui.vis.act.fbRecBtns[2].prop("checked", (!rec._rt || rec._rt.fb !== 2 ? false : true)).button("refresh");
  
  // (3) Manage recommended activities:
  var scaleMe =  // TODO: Make this scale thing more general.
    d3.scale.linear().
    domain(CONST.vis.gridAbs.scales.y).
    range(["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]));
  
  for (var i=0, ni=state.vis.act.rsp.rec.length; i < ni; i++) {
    var recTmp = state.vis.act.rsp.rec[i];
    var spanCell = ui.vis.act.recLst.children[i+2].children[0];  // +2 to skip to the recommended activities
    spanCell.style.backgroundColor = scaleMe(getMe().state.activities[recTmp.topicId][recTmp.resourceId][recTmp.activityId].values[getRepLvl().id]);
  }
  
  /*
  var div = $$("div", ui.vis.act.recLst)
  var scaleMe =
    d3.scale.linear().
    domain(CONST.vis.gridAbs.scales.y).
    range(["#eeeeee"].concat(colorbrewer.PuRd[data.vis.color.binCount - 1]));
  $$("span", div, null, "grid-cell", "&nbsp;&nbsp;&nbsp;&nbsp;").style.backgroundColor = scaleMe(getMe().state.activities[rec.topicId][rec.resourceId][rec.activityId].values[getRepLvl().id]);
  $$("span", div, null, null, "2." + (i+1) + ". " + act.name);
  div.onclick = function (i) {
    return function (e) {
      if (ui.vis.act.recLstSel !== null) $clsRem(ui.vis.act.recLstSel, "sel");
      
      var div = $evtTgt(e);
      if (div.nodeName.toLowerCase() !== "div") div = div.parentNode;  // in case a nested span element has been clicked
      $clsAdd(div, "sel");
      ui.vis.act.recLstSel = div;
      
      actLoadRec(i);
    };
  };
  */
  
  // (6) Log:
  log(
    "action"                           + CONST.log.sep02 + "activity-load-recommended" + CONST.log.sep01 +
    "activity-original-topic-id"       + CONST.log.sep02 + getTopic().id               + CONST.log.sep01 +
    "activity-original-resource-id"    + CONST.log.sep02 + state.vis.act.resId         + CONST.log.sep01 +
    "activity-original-id"             + CONST.log.sep02 + getAct().id                 + CONST.log.sep01 +
    "activity-recommended-topic-id"    + CONST.log.sep02 + rec.topicId                 + CONST.log.sep01 +
    "activity-recommended-resource-id" + CONST.log.sep02 + rec.resourceId              + CONST.log.sep01 +
    "activity-recommended-id"          + CONST.log.sep02 + rec.activityId              + CONST.log.sep01 +
    "recommendation-id"                + CONST.log.sep02 + rec.recommendationId        + CONST.log.sep01 +
    "recommendation-rank"              + CONST.log.sep02 + rec.rank                    + CONST.log.sep01 +
    "recommendation-score"             + CONST.log.sep02 + rec.score,
    true
  );
}


// ------------------------------------------------------------------------------------------------------
/**
 * Loads the original activity (typically accessed from the recommended-activities side bar).
 */
function actLoadRecOriginal() {
  if (state.vis.act.recIdx === -1) return;
  
  // (1) Update the activity grids:
  if (state.vis.act.recIdx >= 0) {
    var res = getRes(state.vis.act.rsp.rec[state.vis.act.recIdx].resourceId);
    if (res.updateStateOn && (res.updateStateOn.winClose || (res.updateStateOn.winCloseIfAct && state.vis.act.doUpdState))) {
      vis.loadingShow();
      actUpdGrids(true, function () { vis.loadingHide(); });
    }
  }
  
  // (2) The rest:
  state.vis.act.recIdx = -1;
  
  if (ui.vis.act.recLstSel !== null) $clsRem(ui.vis.act.recLstSel, "sel");
  
  $clsAdd(ui.vis.act.recLst.children[0], "sel");
  ui.vis.act.recLstSel = ui.vis.act.recLst.children[0];
  
  $hide(ui.vis.act.fbRecCont);
  ui.vis.act.fbRecTxt.innerHTML = "";
  
  $show(ui.vis.act.frame);
  $hide(ui.vis.act.frameRec);
  
  log(
    "action"               + CONST.log.sep02 + "activity-load-original" + CONST.log.sep01 +
    "activity-topic-id"    + CONST.log.sep02 + getTopic().id            + CONST.log.sep01 +
    "activity-resource-id" + CONST.log.sep02 + state.vis.act.resId      + CONST.log.sep01 +
    "activity-id"          + CONST.log.sep02 + getAct().id,
    true
  );
}

/*
 * Shows the help window
 */
function helpDialogShow(origin,x,y){
    $removeChildren(ui.vis.helpDlgTitle);
    if (origin === "") {helpTitle = ""; helpSrc = "";}
    //$$("span", ui.vis.helpDlgTitle, "help-title-text", "", helpTitle);
    $($$input("button", ui.vis.helpDlgTitle, "btn-act-lst-close", "small-btn", "close")).button().click(helpDialogHide);
    
 
    ui.vis.helpDlg.style.width = "250px";
    ui.vis.helpDlg.style.height = "150px";
    
    //ui.vis.helpDlgCont.innerHTML='<object type="text/html" data="'+helpSrc+'" ></object>';
    ui.vis.helpDlgCont.innerHTML = generateHelp(origin);
    
    $show(ui.vis.helpDlg);
    
    ui.vis.helpDlg.style.left = (x + 5) + "px";
    ui.vis.helpDlg.style.top  = (y + 5) + "px";
    
    
}

function helpDialogHide(){
    $hide(ui.vis.helpDlg);
}

// ------------------------------------------------------------------------------------------------------
/**
 * Shows the actitivies list (i.e., the overlay).
 * 
 * 'state.vis.topicIdx' should be set before this function is invoked.
 */
function actLstShow(doMe, doVs, doGrp) {
  
  state.vis.lastCellSel.doMe = doMe;
  state.vis.lastCellSel.doVs = doVs;
  state.vis.lastCellSel.doGrp = doGrp;
  state.vis.lastCellSel.cellIdxSel = state.vis.grid.cellIdxSel;
  state.vis.lastCellSel.cellSel = state.vis.grid.cellSel;
  state.vis.lastCellSel.topicIdx = state.vis.grid.topicIdx;
  state.vis.lastCellSel.gridName = state.vis.grid.name;
  
  if (state.vis.topicIdx === 0) actLstHide();  // the "average" topic has been clicked
  $removeChildren(ui.vis.actLst.cont);
  
  var topic     = getTopic();
  var me        = getMe();
  var grp       = getGrp();
  var res       = getRes();
  var resNames  = $map(function (x) { return x.name; }, data.resources.slice(1));
  var title     = "";  // "<span class=\"info\">Activities</span>";
  
  $($$input("button", ui.vis.actLst.cont, "btn-act-lst-close", "small-btn", "close")).button().click(actLstHide);
  
  // (1) Generate the activities grid:
  // (1.1) All resources:
  if (state.vis.resIdx < 0) {
    switch (state.vis.mode) {
      // (1.1.1) Group comparison mode:
      case CONST.vis.mode.grp:
        if (doMe)  visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me",        me,           null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]);                                                    }, data.resources), true,  false), CONST.vis.gridAbsAct, title, null,                       false, false,                       0,                           state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barAbsMini, resNames, true,  false, false, false);
        if (doVs)  visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me vs grp", me,           grp,      [],          $map(function (x) { return CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]); }, data.resources), false, false), CONST.vis.gridDevAct, title, null,                       false, false,                       0,                           state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, false, false);
        if (doGrp) visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:grp",       grp,          null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp[data.vis.color.binCount - 1]);                                                   }, data.resources), false, false), CONST.vis.gridAbsAct, title, null,                       false, false,                       0,                           state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barAbsMini, resNames, true,  false, false, false);
        break;
      
      // (1.1.1) Individual comparison mode:
      case CONST.vis.mode.ind:
        visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me",        me,           null,     ["Me"],      $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                   }, data.resources), true,  false ), CONST.vis.gridAbsAct, title, null,                       false, false,                       0,                           state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barAbsMini, resNames, true,  false, false, false);
        break;
    }
  }  
  // (1.2) One resource:
  else {
    var act        = (topic.activities ? topic.activities[res.id] || [] : []);
    var topicNames = [topic.name].concat($map(function (x) { return x.name; }, act));
    var topicMaxW  = svgGetMaxTextBB(topicNames).width + 10;
    switch (state.vis.mode) {
      // (1.2.1) Group comparison mode:
     
      case CONST.vis.mode.grp:
        // Non-AVG resource-focus (e.g., questions, examples, etc.):
        if (res.id !== "AVG") {
          title = "";
          var seriesNames = ["Me", "Me vs group", "Group"];
          var colorScales = [
            CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]),
            CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]),
            CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1])
          ];
          if(state.args.uiGridMeGrpVis || state.args.uiGridGrpVis) {
              if (doMe)    visGenGrid(ui.vis.actLst.cont, visGenGridDataOneRes_act(null,     "act:me",          me,           null,     seriesNames,  colorScales,                                                                                                                                                               true,  false), CONST.vis.gridDevAct, title, null,  false, true,   0, state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, true , false);
              if (doVs)    visGenGrid(ui.vis.actLst.cont, visGenGridDataOneRes_act(null,     "act:me vs grp",   me,           grp,      seriesNames,  colorScales,                                                                                                                                                               true,  false), CONST.vis.gridDevAct, title, null,  false, true,   0, state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, true , false);
              if (doGrp)   visGenGrid(ui.vis.actLst.cont, visGenGridDataOneRes_act(null,     "act:grp",         grp,          null,     seriesNames,  $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp[data.vis.color.binCount - 1]);                                                      }, data.resources), true,  false), CONST.vis.gridAbsAct, title, null,  false, true,   0, state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, true , false);              
          }else{
                           visGenGrid(ui.vis.actLst.cont, visGenGridDataOneRes_act(null,     "act:me",          me,           null,     seriesNames,  colorScales,                                                                                                                                                               true,  false), CONST.vis.gridDevAct, title, null,  false, true,   0, state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, true , false);
          }
        }
        // AVG resource-focus:
        else{
            // @@@@ 
            if(state.args.uiGridMeGrpVis || state.args.uiGridGrpVis) {
//                visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me vs grp", me,           null,      [],          $map(function (x) { return CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]); }, data.resources), true, false), CONST.vis.gridDevAct, title, null,                       false, false,                       0,                           state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, false, false);
                if (doMe)  visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me",          me ,          null,      [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]);                                                       }, data.resources), true,  false), CONST.vis.gridAbsAct, title, null,  false, false,  0, state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, false, false);
                if (doVs)  visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me vs grp",   me ,          grp ,      [],          $map(function (x) { return CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]); }, data.resources), false, false), CONST.vis.gridDevAct, title, null,  false, false,  0, state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, false, false);
                if (doGrp) visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:grp",         grp,          null,      [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp[data.vis.color.binCount - 1]);                                                      }, data.resources), false, false), CONST.vis.gridAbsAct, title, null,  false, false,  0, state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, false, false);

            }
                
            else{
                
                           visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me",          me,           null,      [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]);                                                       }, data.resources), true,  false), CONST.vis.gridAbsAct, title, null,  false, false,  0, state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, false, false);
            }                
        }
        break;
      
      // (1.2.2) Individual comparison mode:
      case CONST.vis.mode.ind:
        // Non-AVG resource-focus (e.g., questions, examples, etc.):
        if (res.id !== "AVG") {
                           visGenGrid(ui.vis.actLst.cont, visGenGridDataOneRes_act(null,     "act:me",          me,           null,     ["Me"],       $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                    }, data.resources), true,  false ), CONST.vis.gridAbsAct, title, null, false, true,   0, state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barDevMini, resNames, true,  false, true , false);
        }
        
        // AVG resource-focus:
        else               visGenGrid(ui.vis.actLst.cont, visGenGridDataAllRes_act(null,     "act:me",          me,           null,     ["Me"],       $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                    }, data.resources), true,  false ), CONST.vis.gridAbsAct, title, null, false, false,  0, state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 0, true,  /*BarChart*/null, CONST.vis.barAbsMini, resNames, true,  false, false, false);
        
        break;
    }
    
    
  }
  
  // (2) Align the list:
  // cell = d3.select("g[data-cell-idx='" + state.vis.grid.cellIdxSel + "']");
  // state.vis.grid.cellSel[0]
  
  var grid = null;
  if (doMe)         grid = ui.vis.grid.cont.me.childNodes[0];
  if (doVs && !res) grid = ui.vis.grid.cont.me.childNodes[1];
  if (doVs &&  res) grid = ui.vis.grid.cont.me.childNodes[0];
  if (doGrp && !res)  grid = ui.vis.grid.cont.grp.childNodes[0];
  if (doGrp && res){
      //if(res.id !== 'AVG') 
          //grid = ui.vis.grid.cont.grp.childNodes[0];
      //else 
      grid = ui.vis.grid.cont.me.childNodes[0];
  }

  if (grid) {
    var y = $getCoords(grid).y2 - 32 - (((!res && doMe) || (res && (doMe || doVs || doGrp))) && state.args.uiGridTimelineVis ? (state.vis.mode === CONST.vis.mode.ind && state.vis.resIdx >= 0 ? 25 : 30) : 0);
    
    $setPosCenter(ui.vis.actLst.cont,  false, ui.vis.actLst.topicCellX[state.vis.topicIdx - 1] + $getCoords($("#grids")[0]).x1, y,      true );
    $setPosCenter(ui.vis.actLst.arrow, false, ui.vis.actLst.topicCellX[state.vis.topicIdx - 1] + $getCoords($("#grids")[0]).x1, y - 15, false);
  }
  
}


// ------------------------------------------------------------------------------------------------------
function actLstHide() {
  state.vis.grid.cellIdxSel = -1;
  state.vis.grid.cellSel    = null;
  state.vis.topicIdx        = -1;
  state.vis.grid.name       = null;
//  state.vis.lastCellSel.doMe = false;
//  state.vis.lastCellSel.doVs = false;
//  state.vis.lastCellSel.doGrp = false;
//  state.vis.lastCellSel.cellIdxSel = -1;
//  state.vis.lastCellSel.cellSel = null;
//  state.vis.lastCellSel.topicIdx = -1;
//  state.vis.lastCellSel.gridName = null;
  $hide(ui.vis.actLst.cont);
  $hide(ui.vis.actLst.arrow);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Opens the specified activity.
 * 
 * - http://adapt2.sis.pitt.edu/quizjet/quiz1.jsp?rdfID=jvariables1&act=Variables&sub=jVariables1&app=25&grp=IS172013Spring&usr=peterb&sid=7EA4F
 */
function actOpen(resId, actIdx) {

  var topic = getTopic();
  var act = topic.activities[resId][actIdx];
  var res = getRes(resId);
  
  state.vis.act.act    = act;
  state.vis.act.resId  = resId;
  state.vis.act.actIdx = actIdx;
  
  $hide(ui.vis.act.recLst);
  $hide(ui.vis.act.fbDiffCont);
  $hide(ui.vis.act.fbRecCont);
  $hide(ui.vis.act.frameRec);
  
  
  // TODO
  if(res.dim){
      if(res.dim.w) ui.vis.act.frame.style.width = res.dim.w + "px";
      if(res.dim.w) ui.vis.act.frame.style.height = res.dim.h + "px";
      //ui.vis.act.frameRec.style.width = "930px";
      //ui.vis.act.frameRec.style.width = "930px";
  }else{
      ui.vis.act.frame.style.width = CONST.vis.actWindow.w;
      ui.vis.act.frame.style.width = CONST.vis.actWindow.h;
  }
//  if(resId === 'ae'){
//      
//      ui.vis.act.frame.style.width ="930px";
//      ui.vis.act.frameRec.style.width = "930px";
//  }
  
  $show(ui.vis.act.frame);
  $show(ui.vis.act.cont);
  
  ui.vis.act.title.innerHTML = "Topic: <b>" + topic.name + "</b> &nbsp; &bull; &nbsp; Activity: <b>" + act.name + "</b>";
  ui.vis.act.frame.src = act.url + "&grp=" + state.curr.grp + "&usr=" + state.curr.usr + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid;
  
  log(
    "action"               + CONST.log.sep02 + "activity-open"     + CONST.log.sep01 +
    "activity-topic-id"    + CONST.log.sep02 + getTopic().id       + CONST.log.sep01 +
    "activity-resource-id" + CONST.log.sep02 + state.vis.act.resId + CONST.log.sep01 +
    "activity-id"          + CONST.log.sep02 + getAct().id,
    true
  );
  
  // NOTE: Old way by opening an activity in a new tab (useful as an example if more tab-code needs to be developed):
  /*
  // remove all tabs after the second one:
  for (var i = 3; i <= ui.nav.tabs.cnt; i++) {
    ui.nav.tabs.tabs.find(".ui-tabs-nav").find("#nav-tabs-tab-" + i + "-li").remove();
    ui.nav.tabs.tabs.find("#nav-tabs-tab-" + i).remove();
  }
  ui.nav.tabs.tabs.tabs("refresh");
  ui.nav.tabs.cnt = 2;
  
  // add the new tab:
  ui.nav.tabs.tabs.find(".ui-tabs-nav").append($("<li id='nav-tabs-tab-3-li'><a href='#nav-tabs-tab-3'>" + name + "</a></li>"));
  ui.nav.tabs.tabs.append("<div id='nav-tabs-tab-3'></div>");
  ui.nav.tabs.tabs.tabs("refresh");
  ui.nav.tabs.tabs.tabs("option", "active", 2);
  ui.nav.tabs.cnt = 3;
  
  // load the activity:
  var frame = $$("frame", $_("nav-tabs-tab-3"), null, "act");
  frame.src = "http://adapt2.sis.pitt.edu/quizjet/quiz1.jsp?rdfID=jvariables1&act=Variables&sub=jVariables1&app=25&grp=IS172013Spring&usr=peterb&sid=7EA4F";
  */
}


// ------------------------------------------------------------------------------------------------------
/**
 * Updates the activities grid. This function can request the new state or assume the current state 
 * already reflects any changes.
 */
function actUpdGrids(doReqState, fnCb) {
  if (doReqState) {
    var uri = CONST.uriServer + "GetContentLevels?usr=" + state.curr.usr + "&grp=" + state.curr.grp + "&mod=user&sid=" + state.curr.sid + "&cid=" + state.curr.cid + "&lastActivityId=" + state.vis.act.act.id + "&res=-1";
    //$call("GET", uri, null, function () { actUpdGrids_cb(fnCb); }, true, false);
    $call("GET", uri, null, updateLearnerData, true, false);
  }
  else actUpdGrids_cb(fnCb);
}

function updateLearnerData(rsp){
    data.learners[getMe(true)] = rsp.learner;
    
    var me = getMe(false);
    visAugmentData_addAvgTopic ([me]);
    visAugmentData_addAvgRes   ([me]);
    
    actUpdGrids_cb(function () { vis.loadingHide();});
    
    
}


// ----^----
function actUpdGrids_cb(fnCb) {
  var cellIdxSel = state.vis.grid.cellIdxSel;  // hold (a)

  visDo(true, false, false);
  
  if(ui.vis.actLst.cont.style.display !== 'none'){
      actLstShow(state.vis.lastCellSel.doMe,state.vis.lastCellSel.doVs,state.vis.lastCellSel.doGrp);
  }
  
  // Set the appropriate cell as selected:
  state.vis.grid.cellIdxSel = cellIdxSel;  // fetch (a)
  state.vis.grid.cellSel    = d3.select("g[data-cell-idx='" + state.vis.grid.cellIdxSel + "']");
  
  var box = state.vis.grid.cellSel.select(".grid-cell-inner").select(".box");
  box.
    attr("rx", (!visDoVaryCellW() ? 20 : 0)).
    attr("ry", (!visDoVaryCellW() ? 20 : 0)).
    style("stroke-width", (!visDoVaryCellW() ? 1.51 : 1.51)).
    style("stroke", "black");
  
  if (fnCb) fnCb();
}


// ------------------------------------------------------------------------------------------------------
/**
 * Sets comparison mode (group or individual).
 */
function compModeSet(mode) {
  if (mode === state.vis.mode) return;
  
  state.vis.mode = mode;
  
  if (state.args.uiGridActLstMode) actLstHide();
  
  visDo(true, true, true);
  
  log("action" + CONST.log.sep02 + "comparison-mode-set", true);
}

/**
 * Shows or hide comparison and group grids
 */
function comparisonVisible(showGrp, showMeVsGrp, showOthers) {
  if (state.args.uiGridActLstMode) actLstHide();
  state.args.uiGridGrpVis = showGrp;
  state.args.uiGridMeGrpVis = showMeVsGrp;
  state.args.uiGridOthersVis = showOthers;

  visDo(true, true, true);
  
  log("action" + CONST.log.sep02 + "comparison-visible("+(showGrp ? "1" : "0")+","+(showMeVsGrp ? "1" : "0")+","+(showOthers ? "1" : "0")+")", true);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Returns the currently selected activity (original, not recommended one).
 */
function getAct() {
  return state.vis.act.act;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Returns the currently selected recommended activity (not the original one).
 */
function getActRec() {
  var rec = getRec();
  if (rec === null) return null;
  
  var topic = null;
  for (var j=0, nj=data.topics.length; j < nj; j++) { if (data.topics[j].id === rec.topicId) topic = function (j) { return data.topics[j]; }(j); }
  if (topic === null) return null;
  
  var act = null;
  for (var j=0, nj=topic.activities[rec.resourceId].length; j < nj; j++) { if (topic.activities[rec.resourceId][j].id === rec.activityId) act = function (j) { return topic.activities[rec.resourceId][j]; }(j); }
  if (act === null) return null;
  
  return act;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Returns the currently selected group object.  Note, that to get learners which make up that group 
 * you use the 'getOthers()' function.
 */
function getGrp() {
  return data.groups[$_("tbar-grp").selectedIndex];
}


// ------------------------------------------------------------------------------------------------------
/**
 * Returns the learner object of me (i.e., the learner viewing the visualization) or the index of that 
 * learner in the 'data.learner' array.
 */
function getMe(doRetIdx) {
  for (var i=0, ni=data.learners.length; i < ni; i++) {
    var l = data.learners[i];
    if (data.learners[i].id === data.context.learnerId) return (doRetIdx ? i : l);
  }
  return (doRetIdx ? -1 : null);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Return the index (0-based) of the current student (i.e., "me") in the "others" ('getOthers()') array.
 */
function getMeInGrpIdx() {
  var id = getMe(false).id;
  var others = getOthers();
  for (var i=0, ni=others.length; i < ni; i++) {
    if (others[i].id === id) return i;
  }
  return -1;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Returns the list of learners who make up the currently selected group.
 */
function getOthers() {
  var grp = getGrp();
  
  var res = [];
  for (var i=0, ni=data.learners.length; i < ni; i++) {
    var l = data.learners[i];
    if (jQuery.inArray(l.id, grp.learnerIds) >= 0) res.push(l);
  }
  return res;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Return the currently selected recommended activity.
 */
function getRec() {
  return (state.vis.act.recIdx === -1 ? null : state.vis.act.rsp.rec[state.vis.act.recIdx]);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Return the resource with the specified ID.  If 'id' is not specified the current resource is returned
 * instead.
 */
function getRes(id) {
  if (!id) return data.resources[state.vis.resIdx];
  
  var res = null;
  $map(function (x) { if (x.id === id) res = x; }, data.resources);
  return res;
}


// ------------------------------------------------------------------------------------------------------
function getRepLvl() {
  return data.reportLevels[$_("tbar-rep-lvl").value];
}


// ------------------------------------------------------------------------------------------------------
/**
 * Returns the currently selected topic.
 */
function getTopic() {
  return (state.vis.topicIdx === -1 ? null : data.topics[state.vis.topicIdx]);
}


// ------------------------------------------------------------------------------------------------------
function grpSet() {
  if (state.args.uiGridActLstMode) actLstHide();
  
  visDo(true, true, true);
  
  log("action" + CONST.log.sep02 + "group-set", true);
}


// ------------------------------------------------------------------------------------------------------
function grpSetCellH(h) {
  state.vis.otherIndCellH = parseInt(h);
  
  if (getRes()) visDo(false, false, true);
}


// ------------------------------------------------------------------------------------------------------
function init() {
  stateArgsSet01();
  
  log(
    "action"                 + CONST.log.sep02 + "app-start"                   + CONST.log.sep01 +
    "ui-tbar-vis"            + CONST.log.sep02 + state.args.uiTBarVis          + CONST.log.sep01 +
    "ui-tbar-mode-vis"       + CONST.log.sep02 + state.args.uiTBarModeVis      + CONST.log.sep01 +
    "ui-tbar-rep-lvl-vis"    + CONST.log.sep02 + state.args.uiTBarModeVis      + CONST.log.sep01 +
    "ui-tbar-topic-size-vis" + CONST.log.sep02 + state.args.uiTBarTopicSizeVis + CONST.log.sep01 +
    "ui-tbar-grp-vis"        + CONST.log.sep02 + state.args.uiTBarGrpVis       + CONST.log.sep01 +
    "ui-tbar-res-vis"        + CONST.log.sep02 + state.args.uiTBarResVis,
    false
  );
  loadData();
  
}


// ------------------------------------------------------------------------------------------------------
function initUI() {
  // (1) The actual UI:
  $(document).ready(function() {
    // (1.1) Hide elements of the toolbar:
    if (!state.args.uiTBarVis) {
      $("body").addClass("tbar-0");
      $("#tbar").hide();
    }
    else {
      $("body").addClass("tbar-1");
      $("#tbar").show();
      
      if (!state.args.uiTBarModeVis      ? $("#tbar-mode-cont")       .hide() : $("#tbar-mode-cont")       .show());
      if (!state.args.uiTBarRepLvlVis    ? $("#tbar-rep-lvl-cont")    .hide() : $("#tbar-rep-lvl-cont")    .show());
      if (!state.args.uiTBarTopicSizeVis ? $("#tbar-topic-size-cont") .hide() : $("#tbar-topic-size-cont") .show());
      if (!state.args.uiTBarGrpVis       ? $("#tbar-grp-cont")        .hide() : $("#tbar-grp-cont")        .show());
      if (!state.args.uiTBarResVis       ? $("#tbar-res-cont")        .hide() : $("#tbar-res-cont")        .show());
      // @@@@@
      $("#tbar-grp-cell-h").hide();
      $("#tbar-grp-cell-h-unit").hide();
    }
    
    // (1.2) Tooltips:
    $(document).tooltip();
    
    // (1.3) Toolbar:
    if(state.args.uiTBarModeGrpChk){
        $("#tbar-mode-01")[0].checked = true;
        $("#tbar-mode-02")[0].checked = false;
    }else{
        $("#tbar-mode-01")[0].checked = false;
        $("#tbar-mode-02")[0].checked = true;
    }
 
    $("#tbar-mode").buttonset();
    $("#tbar-mode-01").click(function () {
        comparisonVisible(CONST.comparison.grpActive, CONST.comparison.meGrpActive, CONST.comparison.othersActive);
        //compModeSet(CONST.vis.mode.grp); 
    });
    $("#tbar-mode-02").click(function () { 
        comparisonVisible(false, false, false);
        //compModeSet(CONST.vis.mode.ind); 
    });
    
    $("#tbar-grp-cell-h")[0].selectedIndex = state.vis.otherIndCellH - CONST.vis.otherIndCellH.min;

    // (1.4) Grids:
    ui.vis.grid.cont.me     = $("#grid-me")     [0];
    ui.vis.grid.cont.grp    = $("#grid-grp")    [0];
    ui.vis.grid.cont.others = $("#grid-others") [0];
    
    document.onmousedown = function (e) {
      if ($evtMouseBtn(e) === 1) state.isMouseBtn1 = true;
    };
    
    document.onmouseup = function (e) {
      if ($evtMouseBtn(e) === 1) state.isMouseBtn1 = false;
    };
    
    document.oncontextmenu = function (e) {
      return false;
    };
    
    document.onkeyup = function (e) {
      //console.log($evtCode(e));
      switch ($evtCode(e)) {
        case 27:  // ESC
          if (state.vis.act.actIdx !== -1) actClose();
          else actLstHide();
          break;
      }
    };
    
    document.body.onmousewheel = function (e) {
      if (ui.scrollTimer) window.clearTimeout(ui.scrollTimer);
      
      ui.scrollTimer = window.setTimeout(
        function () {
          ui.scrollTimer = null;
          
          log(
            "action" + CONST.log.sep02 + "scroll"                    + CONST.log.sep01 +
            "y"      + CONST.log.sep02 + window.scrollY       + "px" + CONST.log.sep01 +
            "x"      + CONST.log.sep02 + window.scrollX       + "px" + CONST.log.sep01 +
            "scr-h"  + CONST.log.sep02 + window.screen.height + "px" + CONST.log.sep01 +
            "scr-w"  + CONST.log.sep02 + window.screen.width  + "px",
            true
          );
        },
        CONST.scrollTime
      );
    };
    
    
//    document.body.onclick = function (e) {
//      if (state.args.uiGridActLstMode) actLstHide();
//      return false;
//    };
    
    //$("#grids")[0].onclick = function (e) {
    	//if (state.args.uiGridActLstMode) actLstHide();
	//};

    
    // (1.5) Sunburst visualization:
    ui.vis.sunburst = $("#sunburst")[0];
    
    // (1.6) Activities list:
    ui.vis.actLst.cont  = $("#act-lst")[0];
    ui.vis.actLst.arrow = $("#act-lst-arrow")[0];

    
    // (1.7) Activity window:
    ui.vis.act.cont              = $("#act")[0];
    ui.vis.act.cont.onclick      = actClose;
    ui.vis.act.cont.onmousewheel = function (e) {  // prevent scrolling of the main window while scrolling the frame content
      $evtTgt(e).scrollTop -= e.wheelDeltaY;
      $evtPrevDef(e);
    };
    
    ui.vis.act.title      = $("#act-title")        [0];
    ui.vis.act.frame      = $("#act-frame")        [0];
    ui.vis.act.frameRec   = $("#act-frame-rec")    [0];
    ui.vis.act.recLst     = $("#act-rec-lst")      [0];
    ui.vis.act.fbDiffCont = $("#act-fb-diff-cont") [0];
    ui.vis.act.fbDiffTxt  = $("#act-fb-diff-txt")  [0];
    ui.vis.act.fbRecCont  = $("#act-fb-rec-cont")  [0];
    ui.vis.act.fbRecTxt   = $("#act-fb-rec-txt")   [0];
    
    ui.vis.act.recLst.children[0].onclick = actLoadRecOriginal;
    
    ui.vis.act.fbDiffBtns[0] = $("#act-fb-diff-btn-0");  ui.vis.act.fbDiffBtns[0] .click(function (e) { actFbDiff(0); });
    ui.vis.act.fbDiffBtns[1] = $("#act-fb-diff-btn-1");  ui.vis.act.fbDiffBtns[1] .click(function (e) { actFbDiff(1); });
    ui.vis.act.fbDiffBtns[2] = $("#act-fb-diff-btn-2");  ui.vis.act.fbDiffBtns[2] .click(function (e) { actFbDiff(2); });
    
    ui.vis.act.fbRecBtns[0]  = $("#act-fb-rec-btn-0");   ui.vis.act.fbRecBtns[0]  .click(function (e) { actFbRec(0);  });
    ui.vis.act.fbRecBtns[1]  = $("#act-fb-rec-btn-1");   ui.vis.act.fbRecBtns[1]  .click(function (e) { actFbRec(1);  });
    ui.vis.act.fbRecBtns[2]  = $("#act-fb-rec-btn-2");   ui.vis.act.fbRecBtns[2]  .click(function (e) { actFbRec(2);  });
    
    // (1.8) Help dialog:
    ui.vis.helpDlg  = $("#help-dlg")[0];
    ui.vis.helpDlgTitle  = $("#help-dlg-title")[0];
    ui.vis.helpDlgCont  = $("#help-dlg-cont")[0];

    
    $("#act-fb-diff-cont #act-fb-diff") .buttonset();
    $("#act-fb-rec-cont  #act-fb-rec")  .buttonset();
    
    $("#act-close").button();
    
    $("#act-tbl")[0].onclick   = function (e) { $evtCancelProp(e); };  // prevent closing from onclick events
    $("#act-close")[0].onclick = actClose;
  });
  
  // (2) Reverse color scales (we need this for deviation from average -- colors associated with larger negative values should be darker and not lighter as is the case by default):
  CONST.vis.colors.grpRev = [];
  for (var i = 3; i <= 9; i++) {
    CONST.vis.colors.grpRev[i] = CONST.vis.colors.grp[i].slice();
    CONST.vis.colors.grpRev[i].reverse();
  }
  
  CONST.vis.colors.spectralRev = [];
  CONST.vis.colors.spectralRev[7] = CONST.vis.colors.spectral[7].slice();
  CONST.vis.colors.spectralRev[7].reverse();
  
  CONST.vis.colors.spectralRev[11] = CONST.vis.colors.spectral[11].slice();
  CONST.vis.colors.spectralRev[11].reverse();
  
  // (3) SVG common filters:
  ui.vis.svgCommon =
    d3.select(document.body).
    append("svg").
    attr("width", 0).
    attr("height", 0);
  
  // (3.1) Filter (blur):
  ui.vis.svgCommon.append("svg:defs").
    append("svg:filter").
    attr("id", "blur").
    append("svg:feGaussianBlur").
    attr("stdDeviation", 1.5);
    
  // (3.2) Filter (shadow):
  var filterShadow = ui.vis.svgCommon.append("svg:defs").
    append("svg:filter").
    attr("id", "shadow");
  filterShadow.append("svg:feGaussianBlur").
    attr("in", "SourceAlpha").
    attr("stdDeviation", 2);
  filterShadow.append("svg:feOffset").
    attr("dx", 0).
    attr("dy", 0).
    attr("result", "offsetblur");
  var feMerge = filterShadow.append("svg:feMerge");
  feMerge.append("svg:feMergeNode");
  feMerge.append("svg:feMergeNode").
    attr("in", "SourceGraphic");
}


// ------------------------------------------------------------------------------------------------------
function loadData() {
  vis.loadingShow();
  
  log("action" + CONST.log.sep02 + "data-load-start", false);
  
  (state.args.dataLive
    ? $call("GET", CONST.uriServer+"GetContentLevels?usr=" + state.curr.usr + "&grp=" + state.curr.grp + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid + "&mod=all&models=" + (state.args.dataReqOtherLearners ? "-1" : "0") + "&avgtop=" + state.args.dataTopNGrp, null, loadData_cb, true, false)
    : $call("GET", "/um-vis/data.js", null, loadData_cb, true, false)
  );
}


// ----^----
function loadData_cb(res) {
  // (1) Process the data:
  data = res;
  
  if (!data.vis.color.value2color) data.vis.color.value2color = function (x) { var y = Math.log(x)*0.25 + 1;  return (y < 0 ? 0 : y); };  // use the logarithm function by default
  
  visAugmentData();
  
  data._rt = {};
  data._rt.topicsOrd = data.topics.slice(0);  // save the original topic order
  
  // (2) Process arguments (fuse those passed through the query string and those passed in the server's response (the latter take precedence):
  stateArgsSet02();
  
  // (3) Init UI:
  initUI();
  //stateLoad();
  
  // (3.1) Toolbar:
  // (3.1.1) Report levels:
  var repLvlSelIdx = -1;  // selected index
  for (var i = 0; i < data.reportLevels.length; i++) { if (data.reportLevels[i].isDefault) repLvlSelIdx = i; }
  for (var i = 0; i < data.reportLevels.length; i++) {
    var rl = data.reportLevels[i];
    var option = $$("option", $_("tbar-rep-lvl"), null, null, rl.name);
    option.value = i;
    
    if ((repLvlSelIdx !== -1 && repLvlSelIdx === i) || (repLvlSelIdx === -1 && state.args.defValRepLvl === rl.id)) option.selected = "selected";
  }
  

  // (3.1.2) Topic size:
  for (var i = 0; i < data.vis.topicSizeAttr.length; i++) {
    var tsa = data.vis.topicSizeAttr[i];
    var option = $$("option", $_("tbar-topic-size"), null, null, tsa[0].toUpperCase() + tsa.substr(1));
    option.value = tsa;
  }
  
  // (3.1.3) Groups:
  var grpSelIdx = -1;  // selected index
  for (var i = 0; i < data.groups.length; i++) { if (data.groups[i].isDefault) grpSelIdx = i; }
  for (var i = 0; i < data.groups.length; i++) {
    var grp = data.groups[i];
    var option = $$("option", $_("tbar-grp"), null, null, grp.name);
    option.value = i;
    
    if ((grpSelIdx !== -1 && grpSelIdx === i) || (grpSelIdx === -1 && state.args.defValGrpIdx === i)) option.selected = "selected";
  }
  
  
  // (3.1.4) Resources:
  var resSelIdx = -1;  // selected index
  for (var i = 0; i < data.resources.length; i++) { if (data.resources[i].isDefault) resSelIdx = i; }
  for (var i = 0; i < data.resources.length; i++) {
    var res = data.resources[i];
    var option = $$("option", $_("tbar-res"), null, null, res.name);
    option.value = i;
    
    if ((resSelIdx !== -1 && resSelIdx === i) || (resSelIdx === -1 && state.args.defValResId === res.id)) {
      option.selected = "selected";
      resSet(i+2, false, false);  // +2 because this is the index in the drop-down list and there always are two leading items
    }
  }
  
  // (4) Grids:
  visDo(true, true, true);
  
  vis.loadingHide();
  
  log("action" + CONST.log.sep02 + "data-load-end", false);
  log("action" + CONST.log.sep02 + "app-ready",     true );
}


// ------------------------------------------------------------------------------------------------------
function loadDataOthers() {
  
  actLstHide();
  
  var btn = $("#btn-others-load");
  btn.prop("disabled", true);
  var action = 'load-others-list';
  if(btn.attr("value").substring(0,6) === 'Update') action = 'update-others-list';
  
  btn.attr("value", "Loading...");
  

  $call("GET", CONST.uriServer+"GetContentLevels?usr=" + state.curr.usr + "&grp=" + state.curr.grp + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid + "&mod=all&avgtop=" + state.args.dataTopNGrp + "&models=-1", null, loadDataOthers_cb, true, false);
  
  log("action" + CONST.log.sep02 + action, true);
}


// ----^----
function loadDataOthers_cb(res) {
  state.args.dataReqOtherLearners = true;
  
  data.learners = res.learners;  // ... = res
  visAugmentData_addAvgTopic (data.learners);
  visAugmentData_addAvgRes   (data.learners);
  
  visDo(false, false, true);
  
  var btn = $("#btn-others-load");
  btn.prop("disabled", false);
  btn.attr("value", "Update other learners");
}


// ------------------------------------------------------------------------------------------------------
/**
 * Requests the action provided to be logged on the server.  Context information can be added as well.
 */
function log(action, doAddCtx) {
  var uri = CONST.uriServer + "TrackAction?" +
    "usr="    + state.curr.usr + "&" +
    "grp="    + state.curr.grp + "&" +
    "sid="    + state.curr.sid + "&" +
    "cid="    + state.curr.cid + "&" +
    "action=" + action         +
      (doAddCtx
        ? CONST.log.sep01 +
          "ctx-comparison-mode-name"      + CONST.log.sep02 + (state.vis.mode === CONST.vis.mode.grp ? "grp" : "ind")            + CONST.log.sep01 +
          "ctx-report-level-id"           + CONST.log.sep02 + getRepLvl().id                                                     + CONST.log.sep01 +
          "ctx-topic-size-attribute-name" + CONST.log.sep02 + state.vis.topicSize.attr                                           + CONST.log.sep01 +
          "ctx-group-name"                + CONST.log.sep02 + getGrp().name                                                      + CONST.log.sep01 +
          "ctx-resource-id"               + CONST.log.sep02 + (state.vis.resIdx >= 0 ? data.resources[state.vis.resIdx].id : "")
        : ""
      );
  
  $call("GET", uri, null, null, true, false);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Sets report level (i.e., progress, knowledge, etc.)
 */
function repLvlSet() {
  if (state.args.uiGridActLstMode) actLstHide();
  
  //sortMe();
  //sortOthers();
  visDo(true, true, true);
  
  log("action" + CONST.log.sep02 + "report-level-set", true);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Sets resource (i.e., switches between the All-Resources and Resource-Focus modes).
 */
function resSet(idx, doRefreshVis, doLog) {
  state.vis.resIdx = idx - 2;  // there are two entries in the combo box before the first actual resource
  if (state.vis.resIdx >= 0) {
    data.learners.sort(function (a,b) { a.state.topics[data.topics[0].id].values[getRes().id][getRepLvl().id] - b.state.topics[data.topics[0].id].values[getRes().id][getRepLvl().id]; });
  }
  
  if (state.args.uiGridActLstMode) actLstHide();
  
  if (doRefreshVis) visDo(true, true, true);
  
  if (doLog) log("action" + CONST.log.sep02 + "resource-set", true); 
}


// ------------------------------------------------------------------------------------------------------
/**
 * Because the data.topics array is used to access the actual resource values for the purpose of 
 * visualization, it is enough to sort that array.
 * 
 * [not used any more]
 */
function sortMe() {
  ui.vis.grid.me.tbar.sortByIdx  = ui.vis.grid.me.tbar.sortBy.selectedIndex;
  ui.vis.grid.me.tbar.sortDirIdx = ui.vis.grid.me.tbar.sortDir.selectedIndex;
  
  var by    = ui.vis.grid.me.tbar.sortBy.value;
  var isAsc = (ui.vis.grid.me.tbar.sortDir.value === "a");
  
  // (1) Sort:
  if (by === "-") return;
  
  else if (by === "original") {
    data.topics = [data.topics[0]].concat(isAsc ? data._rt.topicsOrd.slice(1) : data._rt.topicsOrd.slice(1).reverse());
  }
  
  else if (by === "topic") {
    var tmp = data._rt.topicsOrd.slice(1);
    tmp.sort(function (a,b) {
      if (a.name > b.name) return (isAsc ?  1 : -1);
      if (a.name < b.name) return (isAsc ? -1 :  1);
      return 0;
    });
    data.topics = [data.topics[0]].concat(tmp);
  }
  
  else {
    var tmp = data._rt.topicsOrd.slice(1);
    var me = getMe(false);
    tmp.sort(function (a,b) {
      return (isAsc
        ? me.state.topics[a.id].values[by][getRepLvl().id] - me.state.topics[b.id].values[by][getRepLvl().id]
        : me.state.topics[b.id].values[by][getRepLvl().id] - me.state.topics[a.id].values[by][getRepLvl().id]
      );
    });
    data.topics = [data.topics[0]].concat(tmp);
  }
  
  log(
    "action"    + CONST.log.sep02 + "me-sort" + CONST.log.sep01 +
    "by"        + CONST.log.sep02 + by        + CONST.log.sep01 +
    "ascending" + CONST.log.sep02 + (isAsc ? 1 : 0),
    true
  );
  
  // (2) Refresh visualization:
  visDo(true, true, true);
  
  /*
  // Example (http://bl.ocks.org/mbostock/3885705):
  var x0 = x.domain(
    data.sort(this.checked
      ? function(a, b) { return b.frequency - a.frequency; }
      : function(a, b) { return d3.ascending(a.letter, b.letter); })
      .map(function(d) { return d.letter; }))
      .copy();
  
  var transition = svg.transition().duration(750),
      delay = function(d, i) { return i * 50; };
  
  transition.selectAll(".bar")
      .delay(delay)
      .attr("x", function(d) { return x0(d.letter); });
  
  transition.select(".x.axis")
      .call(xAxis)
    .selectAll("g")
      .delay(delay);
  */
}


// ------------------------------------------------------------------------------------------------------
/**
 * Sorts other learners (i.e., the rest) by the specified resource.
 * 
 * [not used any more]
 */
function sortOthers() {
  ui.vis.grid.others.tbar.sortByIdx  = ui.vis.grid.others.tbar.sortBy.selectedIndex;
  ui.vis.grid.others.tbar.sortDirIdx = ui.vis.grid.others.tbar.sortDir.selectedIndex;
  
  var by    = ui.vis.grid.others.tbar.sortBy.value;
  var isAsc = (ui.vis.grid.others.tbar.sortDir.value === "a");
  
  data.learners.sort(function (a,b) {
    return (isAsc
      ? a.state.topics[data.topics[0].id].values[by][getRepLvl().id] - b.state.topics[data.topics[0].id].values[by][getRepLvl().id]
      : b.state.topics[data.topics[0].id].values[by][getRepLvl().id] - a.state.topics[data.topics[0].id].values[by][getRepLvl().id]
    );
  });
  
  log(
    "action"    + CONST.log.sep02 + "others-sort" + CONST.log.sep01 +
    "by"        + CONST.log.sep02 + by            + CONST.log.sep01 +
    "ascending" + CONST.log.sep02 + (isAsc ? 1 : 0),
    true
  );
  
  visDo(false, false, true);
}


// ------------------------------------------------------------------------------------------------------
/**
 * These query-string arguments need to be known BEFORE the data has been requested from the server.
 */
function stateArgsSet01() {
  qs = $getQS();
  
  // Session:
  state.curr.usr = qs.usr;
  state.curr.grp = qs.grp;
  state.curr.sid = qs.sid;
  state.curr.cid = qs.cid;
  
  // Data:
  state.args.dataLive             = (qs["data-live"] === "0" ? false : true);
  state.args.dataTopNGrp          = (isNaN(parseInt(qs["data-top-n-grp"])) || parseInt(qs["data-top-n-grp"]) <= 0 ? CONST.defTopN : parseInt(qs["data-top-n-grp"]));
  state.args.dataReqOtherLearners = (qs["data-req-other-learners"] === "1" ? true : false);
  
  
}


// ------------------------------------------------------------------------------------------------------
/**
 * These query-string arguments need (or can) to be known AFTER the data has been requested from the
 * server.
 */
function stateArgsSet02() {
  qs = $getQS();
  
  // Default values:
  state.args.defValRepLvl           = qs["def-val-rep-lvl-id"];
  state.args.defValGrpIdx           = parseInt(qs["def-val-grp-idx"]);
  state.args.defValResId            = qs["def-val-res-id"];
  
  // UI: Toolbar:
  state.args.uiTBarVis              = (qs["ui-tbar-vis"]            === "0" ? false : true);

  state.args.uiTBarModeVis          = (qs["ui-tbar-mode-vis"]       === "0" ? false : true);
  state.args.uiTBarModeGrpChk       = (qs["ui-tbar-mode-grp-chk"]   === "0" ? false : true);
  state.args.uiTBarRepLvlVis        = (qs["ui-tbar-rep-lvl-vis"]    === "0" ? false : true);
  state.args.uiTBarTopicSizeVis     = (qs["ui-tbar-topic-size-vis"] === "0" ? false : true);
  state.args.uiTBarGrpVis           = (qs["ui-tbar-grp-vis"]        === "0" ? false : true);
  state.args.uiTBarResVis           = (qs["ui-tbar-res-vis"]        === "0" ? false : true);
  
  // UI: Grids:
  state.args.uiGridAllHeadMeVis     = (qs["ui-grid-all-head-me-vis"]     === "0" ? false : true);
  state.args.uiGridAllHeadMeGrpVis  = (qs["ui-grid-all-head-me-grp-vis"] === "0" ? false : true);
  state.args.uiGridAllHeadGrpVis    = (qs["ui-grid-all-head-grp-vis"]    === "0" ? false : true);
  state.args.uiGridAllHeadOthersVis = (qs["ui-grid-all-head-others-vis"] === "0" ? false : true);
  
  state.args.uiGridOneHeadMeVis     = (qs["ui-grid-one-head-me-vis"]     === "0" ? false : true);
  state.args.uiGridOneHeadOthersVis = (qs["ui-grid-one-head-others-vis"] === "0" ? false : true);
  
  state.args.uiGridMeVis            = (qs["ui-grid-me-vis"]     === "0" ? false : true);
  state.args.uiGridMeGrpVis         = (qs["ui-grid-me-grp-vis"] === "0" ? false : true);
  state.args.uiGridGrpVis           = (qs["ui-grid-grp-vis"]    === "0" ? false : true);
  state.args.uiGridOthersVis        = (qs["ui-grid-others-vis"] === "0" ? false : true);
  
  state.args.uiGridTimelineVis      = (qs["ui-grid-timeline-vis"]  === "0" ? false : true);
  //state.args.uiGridTimelineTitle    = "Week";
  state.args.uiGridTimelineTitle    = "";
  state.args.uiGridActLstMode       = (qs["ui-grid-act-lst-mode"]  === "0" ? false : true);
  
  state.args.uiShowHelp             = (qs["ui-show-help"]  === "1" ? true : false);
  
  // @@@@
  // TODO overwrite parameters with the ones in the data: 
  // data.vis.ui.params.group and data.vis.ui.params.user (in this order)
  
  // Overwrite Parameters defined for the group 
  if(data.vis.ui.params.group){   
      state.args.defValRepLvl           = (data.vis.ui.params.group.defValRepLvlId != undefined ? data.vis.ui.params.group.defValRepLvlId : state.args.defValRepLvl);
      state.args.defValGrpIdx           = (data.vis.ui.params.group.defValGrpIdx != undefined ? data.vis.ui.params.group.defValGrpIdx : state.args.defValGrpIdx);
      state.args.defValResId            = (data.vis.ui.params.group.defValResId != undefined ? data.vis.ui.params.group.defValResId : state.args.defValResId);
      state.args.uiTBarVis              = (data.vis.ui.params.group.uiTBarVis != undefined ? data.vis.ui.params.group.uiTBarVis : state.args.uiTBarVis);
      state.args.uiTBarModeVis          = (data.vis.ui.params.group.uiTBarModeVis != undefined ? data.vis.ui.params.group.uiTBarModeVis : state.args.uiTBarModeVis);
      state.args.uiTBarModeGrpChk       = (data.vis.ui.params.group.uiTBarModeGrpChk != undefined ? data.vis.ui.params.group.uiTBarModeGrpChk : state.args.uiTBarModeGrpChk);

      state.args.uiTBarRepLvlVis        = (data.vis.ui.params.group.uiTBarRepLvlVis != undefined ? data.vis.ui.params.group.uiTBarRepLvlVis : state.args.uiTBarRepLvlVis);
      state.args.uiTBarTopicSizeVis     = (data.vis.ui.params.group.uiTBarTopicSizeVis != undefined ? data.vis.ui.params.group.uiTBarTopicSizeVis : state.args.uiTBarTopicSizeVis);
      state.args.uiTBarGrpVis           = (data.vis.ui.params.group.uiTBarGrpVis != undefined ? data.vis.ui.params.group.uiTBarGrpVis : state.args.uiTBarGrpVis);
      state.args.uiTBarResVis           = (data.vis.ui.params.group.uiTBarResVis != undefined ? data.vis.ui.params.group.uiTBarResVis : state.args.uiTBarResVis);
      state.args.uiGridAllHeadMeVis     = (data.vis.ui.params.group.uiGridAllHeadMeVis != undefined ? data.vis.ui.params.group.uiGridAllHeadMeVis : state.args.uiGridAllHeadMeVis);
      state.args.uiGridAllHeadMeGrpVis  = (data.vis.ui.params.group.uiGridAllHeadMeGrpVis != undefined ? data.vis.ui.params.group.uiGridAllHeadMeGrpVis : state.args.uiGridAllHeadMeGrpVis);
      state.args.uiGridAllHeadGrpVis    = (data.vis.ui.params.group.uiGridAllHeadGrpVis != undefined ? data.vis.ui.params.group.uiGridAllHeadGrpVis : state.args.uiGridAllHeadGrpVis);
      state.args.uiGridAllHeadOthersVis = (data.vis.ui.params.group.uiGridAllHeadOthersVis != undefined ? data.vis.ui.params.group.uiGridAllHeadOthersVis : state.args.uiGridAllHeadOthersVis);
      state.args.uiGridOneHeadMeVis     = (data.vis.ui.params.group.uiGridOneHeadMeVis != undefined ? data.vis.ui.params.group.uiGridOneHeadMeVis : state.args.uiGridOneHeadMeVis);
      state.args.uiGridOneHeadOthersVis = (data.vis.ui.params.group.uiGridOneHeadOthersVis != undefined ? data.vis.ui.params.group.uiGridOneHeadOthersVis : state.args.uiGridOneHeadOthersVis);
      state.args.uiGridMeVis            = (data.vis.ui.params.group.uiGridMeVis != undefined ? data.vis.ui.params.group.uiGridMeVis : state.args.uiGridMeVis);
      state.args.uiGridMeGrpVis         = (data.vis.ui.params.group.uiGridMeGrpVis != undefined ? data.vis.ui.params.group.uiGridMeGrpVis : state.args.uiGridMeGrpVis);
      state.args.uiGridGrpVis           = (data.vis.ui.params.group.uiGridGrpVis != undefined ? data.vis.ui.params.group.uiGridGrpVis : state.args.uiGridGrpVis);
      state.args.uiGridOthersVis        = (data.vis.ui.params.group.uiGridOthersVis != undefined ? data.vis.ui.params.group.uiGridOthersVis : state.args.uiGridOthersVis);
      state.args.uiGridTimelineVis      = (data.vis.ui.params.group.uiGridTimelineVis != undefined ? data.vis.ui.params.group.uiGridTimelineVis : state.args.uiGridTimelineVis);
      state.args.uiGridTimelineTitle    = "";
      state.args.uiGridActLstMode       = (data.vis.ui.params.group.uiGridActLstMode != undefined ? data.vis.ui.params.group.uiGridActLstMode : state.args.uiGridActLstMode);
      state.args.uiShowHelp             = (data.vis.ui.params.group.uiShowHelp != undefined ? data.vis.ui.params.group.uiShowHelp : state.args.uiShowHelp);
  }
  if(data.vis.ui.params.user){
      state.args.defValRepLvl           = (data.vis.ui.params.user.defValRepLvlId != undefined ? data.vis.ui.params.user.defValRepLvlId : state.args.defValRepLvl);
      state.args.defValGrpIdx           = (data.vis.ui.params.user.defValGrpIdx != undefined ? data.vis.ui.params.user.defValGrpIdx : state.args.defValGrpIdx);
      state.args.defValResId            = (data.vis.ui.params.user.defValResId != undefined ? data.vis.ui.params.user.defValResId : state.args.defValResId);
      state.args.uiTBarVis              = (data.vis.ui.params.user.uiTBarVis != undefined ? data.vis.ui.params.user.uiTBarVis : state.args.uiTBarVis);
      state.args.uiTBarModeVis          = (data.vis.ui.params.user.uiTBarModeVis != undefined ? data.vis.ui.params.user.uiTBarModeVis : state.args.uiTBarModeVis);
      state.args.uiTBarModeGrpChk       = (data.vis.ui.params.user.uiTBarModeGrpChk != undefined ? data.vis.ui.params.user.uiTBarModeGrpChk : state.args.uiTBarModeGrpChk);

      state.args.uiTBarRepLvlVis        = (data.vis.ui.params.user.uiTBarRepLvlVis != undefined ? data.vis.ui.params.user.uiTBarRepLvlVis : state.args.uiTBarRepLvlVis);
      state.args.uiTBarTopicSizeVis     = (data.vis.ui.params.user.uiTBarTopicSizeVis != undefined ? data.vis.ui.params.user.uiTBarTopicSizeVis : state.args.uiTBarTopicSizeVis);
      state.args.uiTBarGrpVis           = (data.vis.ui.params.user.uiTBarGrpVis != undefined ? data.vis.ui.params.user.uiTBarGrpVis : state.args.uiTBarGrpVis);
      state.args.uiTBarResVis           = (data.vis.ui.params.user.uiTBarResVis != undefined ? data.vis.ui.params.user.uiTBarResVis : state.args.uiTBarResVis);
      state.args.uiGridAllHeadMeVis     = (data.vis.ui.params.user.uiGridAllHeadMeVis != undefined ? data.vis.ui.params.user.uiGridAllHeadMeVis : state.args.uiGridAllHeadMeVis);
      state.args.uiGridAllHeadMeGrpVis  = (data.vis.ui.params.user.uiGridAllHeadMeGrpVis != undefined ? data.vis.ui.params.user.uiGridAllHeadMeGrpVis : state.args.uiGridAllHeadMeGrpVis);
      state.args.uiGridAllHeadGrpVis    = (data.vis.ui.params.user.uiGridAllHeadGrpVis != undefined ? data.vis.ui.params.user.uiGridAllHeadGrpVis : state.args.uiGridAllHeadGrpVis);
      state.args.uiGridAllHeadOthersVis = (data.vis.ui.params.user.uiGridAllHeadOthersVis != undefined ? data.vis.ui.params.user.uiGridAllHeadOthersVis : state.args.uiGridAllHeadOthersVis);
      state.args.uiGridOneHeadMeVis     = (data.vis.ui.params.user.uiGridOneHeadMeVis != undefined ? data.vis.ui.params.user.uiGridOneHeadMeVis : state.args.uiGridOneHeadMeVis);
      state.args.uiGridOneHeadOthersVis = (data.vis.ui.params.user.uiGridOneHeadOthersVis != undefined ? data.vis.ui.params.user.uiGridOneHeadOthersVis : state.args.uiGridOneHeadOthersVis);
      state.args.uiGridMeVis            = (data.vis.ui.params.user.uiGridMeVis != undefined ? data.vis.ui.params.user.uiGridMeVis : state.args.uiGridMeVis);
      state.args.uiGridMeGrpVis         = (data.vis.ui.params.user.uiGridMeGrpVis != undefined ? data.vis.ui.params.user.uiGridMeGrpVis : state.args.uiGridMeGrpVis);
      state.args.uiGridGrpVis           = (data.vis.ui.params.user.uiGridGrpVis != undefined ? data.vis.ui.params.user.uiGridGrpVis : state.args.uiGridGrpVis);
      state.args.uiGridOthersVis        = (data.vis.ui.params.user.uiGridOthersVis != undefined ? data.vis.ui.params.user.uiGridOthersVis : state.args.uiGridOthersVis);
      state.args.uiGridTimelineVis      = (data.vis.ui.params.user.uiGridTimelineVis != undefined ? data.vis.ui.params.user.uiGridTimelineVis : state.args.uiGridTimelineVis);
      state.args.uiGridTimelineTitle    = "";
      state.args.uiGridActLstMode       = (data.vis.ui.params.user.uiGridActLstMode != undefined ? data.vis.ui.params.user.uiGridActLstMode : state.args.uiGridActLstMode);
      state.args.uiShowHelp             = (data.vis.ui.params.user.uiShowHelp != undefined ? data.vis.ui.params.user.uiShowHelp : state.args.uiShowHelp);    
  }
  
  CONST.comparison.grpActive        = state.args.uiGridGrpVis;
  CONST.comparison.meGrpActive      = state.args.uiGridMeGrpVis;
  CONST.comparison.othersActive     = state.args.uiGridOthersVis;
  
  if(!state.args.uiTBarModeGrpChk){
      state.args.uiGridGrpVis = false;
      state.args.uiGridMeGrpVis = false;
      state.args.uiGridOthersVis = false;
  }
  
}


// ------------------------------------------------------------------------------------------------------
/**
 * Loads the app state from cookies.
 */
function stateLoad() {
  var c = $.cookies.get(CONST.appName);
  if (!c) return;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Loads the app state using cookies.
 */
function stateSave() {
  var date = new Date();
  date.setTime(date.getTime() + (CONST.cookies.days*24*60*60*1000));
  
  $.cookies.set(
    CONST.appName,
    {
      // key: value
    },
    { expiresAt: date }
  );
}


// ------------------------------------------------------------------------------------------------------
function svgGetMaxTextBB(T) {
  var ns = "http://www.w3.org/2000/svg";
  var svg = document.createElementNS(ns, "svg");
  document.body.appendChild(svg);
  
  var res = { width:-1, height:-1 };
  for (var i=0, ni=T.length; i < ni; i++) {
    var txt = document.createElementNS(ns, "text");
    txt.appendChild(document.createTextNode(T[i]));
    svg.appendChild(txt);
    
    var bb = txt.getBBox();
    if (bb.width  > res.width ) res.width  = bb.width ;
    if (bb.height > res.height) res.height = bb.height;
  }
  
  document.body.removeChild(svg);
  
  return res;
}


// ------------------------------------------------------------------------------------------------------
function topicSizeSet(idx, attr) {
  if (idx === state.vis.topicSize.idx) return;
  
  state.vis.topicSize.idx  = idx;
  state.vis.topicSize.attr = attr;
  
  if (state.args.uiGridActLstMode) actLstHide();
  
  visDo(true, true, true);
  
  log("action" + CONST.log.sep02 + "topic-size-set", true);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Augments the data received from the server (and stored in the 'data' global variables) by adding 
 * the following things to it:
 * 
 *   - The "Average" topic being the average over all actual topics (an extra grid column)
 *   - The "Average" resource being the per-topic average over all actual resources (an extra grid row)
 * 
 * Note that this is the function which should be inspected first in the case the protocol changes.  I 
 * won't get into the specifics of why I choose to do a questionable thing and add stuff to the actual 
 * data object.  Suffice to say that it makes visualization much much easier later on.
 */
function visAugmentData() {
  // (1) Add the "Average" topic:
  var newTopic = { id: "AVG", name: "OVERALL" };
  
  for (var i=0, ni=data.vis.topicSizeAttr.length; i < ni; i++) {
    newTopic[data.vis.topicSizeAttr[i]] = 0.5;
  }
  
  data.topics.splice(0, 0, newTopic);
  visAugmentData_addAvgTopic(data.learners);
  visAugmentData_addAvgTopic(data.groups);
  
  // (2) Add the "Average" resource:
  data.resources.splice(0, 0, { id: "AVG", name: "OVERALL" });
  visAugmentData_addAvgRes(data.learners);
  visAugmentData_addAvgRes(data.groups);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Add the average topic to each element of the list supplied.  Elements of that list should contain the 
 * state object as defined in the protocol.
 */
function visAugmentData_addAvgTopic(lst) {
  for (var iElem=0, nElem=lst.length; iElem < nElem; iElem++) {
    var elem = lst[iElem];
    var newTopic = { values: {} };
    
    // (1) Sum up over topics per resource per report level:
    for (var iTopic=0, nTopic=data.topics.length; iTopic < nTopic; iTopic++) {
      var topic = data.topics[iTopic];
      if (topic.id === "AVG") continue;
      
      for (var iRes=0, nRes=data.resources.length; iRes < nRes; iRes++) {
        var res = data.resources[iRes];
        if (res.id === "AVG") continue;
      
        if (newTopic.values[res.id] == undefined) newTopic.values[res.id] = {};
        
        for (var iRepLvl=0, nRepLvl=data.reportLevels.length; iRepLvl < nRepLvl; iRepLvl++) {
          var repLvl = data.reportLevels[iRepLvl];
          if (!newTopic.values[res.id][repLvl.id]) newTopic.values[res.id][repLvl.id] = 0;
          
          newTopic.values[res.id][repLvl.id] += elem.state.topics[topic.id].values[res.id][repLvl.id];
        }
      }
    }
    
    // (2) Divide by the number of topics:
    for (var iRes=0, nRes=data.resources.length; iRes < nRes; iRes++) {
      var res = data.resources[iRes];
      if (res.id === "AVG") continue;
      
      for (var iRepLvl=0, nRepLvl=data.reportLevels.length; iRepLvl < nRepLvl; iRepLvl++) {
        var repLvl = data.reportLevels[iRepLvl];
        
        newTopic.values[res.id][repLvl.id] /= (data.topics.length - 1);  // -1 to exclude the "Average" topic which should have already been added
      }
    }
    
    // (3) Associate with the learner:
    elem.state.topics["AVG"] = newTopic;
  }
}


// ------------------------------------------------------------------------------------------------------
/**
 * Add the average resource to each element of the list supplied.  Elements of that list should contain the 
 * state object as defined in the protocol.
 */
function visAugmentData_addAvgRes(lst) {
  for (var iElem=0, nElem=lst.length; iElem < nElem; iElem++) {
    var elem = lst[iElem];
    
    for (var iTopic=0, nTopic=data.topics.length; iTopic < nTopic; iTopic++) {
      var topic = data.topics[iTopic];
      var newRes = {};
      // if the overall value is available in the server data, use this value
      if(elem.state.topics[topic.id].overall){
          //alert(elem.state.topics[topic.id].overall["p"]);
          for (var iRepLvl=0, nRepLvl=data.reportLevels.length; iRepLvl < nRepLvl; iRepLvl++) {
              var repLvl = data.reportLevels[iRepLvl];
              newRes[repLvl.id] = elem.state.topics[topic.id].overall[repLvl.id];   
          }
      }else{ // compute the overall by averaging resource level averages
          // (1) Sum up over resources per report level:
          for (var iRes=0, nRes=data.resources.length; iRes < nRes; iRes++) {
            var res = data.resources[iRes];
            if (res.id === "AVG") continue;
            
            for (var iRepLvl=0, nRepLvl=data.reportLevels.length; iRepLvl < nRepLvl; iRepLvl++) {
              var repLvl = data.reportLevels[iRepLvl];
              if (newRes[repLvl.id] == undefined) newRes[repLvl.id] = 0;
              
              newRes[repLvl.id] += elem.state.topics[topic.id].values[res.id][repLvl.id];
            }
          }
          
          // (2) Divide by the number of resources:
          for (var iRepLvl=0, nRepLvl=data.reportLevels.length; iRepLvl < nRepLvl; iRepLvl++) {
            var repLvl = data.reportLevels[iRepLvl];
            
            newRes[repLvl.id] /= (data.resources.length - 1);  // -1 to exclude the "Average" resource which should have already been added
          }
          
      }
          
      
      // (3) Associate with the topic:
      elem.state.topics[topic.id].values["AVG"] = newRes;
    }
  }
}


// ------------------------------------------------------------------------------------------------------
/**
 * Makes the entire visualization happen. The "me" and "group" part can be refreshed independently 
 * depending on the arguments. This is useful because only the "me" part should be refreshed upon the 
 * learner completing an activity.  Note, that here "me" and "group" do not denote individual grids but
 * rather those grids that "me" (i.e., the current learner) or the "group" (i.e., everyone but me) are 
 * involved in.  Same goes for "others."
 */
function visDo(doMe, doGrp, doOthers) {
  var scroll = { x: window.scrollX, y: window.scrollY };
  
  // (1) Reset:
  if (doMe)     $removeChildren(ui.vis.grid.cont.me);
  if (doGrp)    $removeChildren(ui.vis.grid.cont.grp);
  if (doOthers) $removeChildren(ui.vis.grid.cont.others);
  
  state.vis.grid.cellIdxMax = 0;
  state.vis.grid.cellIdxSel = -1;
  state.vis.grid.cellSel    = null;
  
  var me        = getMe(false);
  var meIdx     = getMeInGrpIdx();
  var grp       = getGrp();
  var others    = getOthers();
  var topic     = getTopic();
  var res       = getRes();
  var resNames  = $map(function (x) { return x.name; }, data.resources);
  var topicMaxW = svgGetMaxTextBB($.map(data.topics, function (x) { return x.name; })).width;
  
  if($_("tbar-grp").selectedIndex > 0){
      othersTitle = getGrp().name + " students";
  }else{
      othersTitle = "Students in the class";
  }
  
  // (2) Grids:
  // (2.1) Prepare "Me" toolbar:
  var tbarMe = null;
  /*
  if (doMe || doGrp) {
    tbarMe = $$("div", null, null, "grid-tbar");
    if (topic === null) {  // topics grid
      // Topic order:
      $$("span", tbarMe, null, null, "Order topics by ");
      var sel = $$("select", tbarMe);
      $$("option", sel, null, null, "Original").value = "original";
      $$("option", sel, null, null, "Name").value = "topic";
      $$("option", sel, null, null, "---").value = "-";
      for (var i=0, ni=data.resources.length; i < ni; i++) {
        $$("option", sel, null, null, data.resources[i].name).value = data.resources[i].id;
      }
      sel.selectedIndex = ui.vis.grid.me.tbar.sortByIdx;
      sel.onchange = sortMe;
      ui.vis.grid.me.tbar.sortBy = sel;
      
      // Topic order direction:
      var sel = $$("select", tbarMe);
      $$("option", sel, null, null, "Low to high").value = "a";
      $$("option", sel, null, null, "High to low").value = "d";
      sel.selectedIndex = ui.vis.grid.me.tbar.sortDirIdx;
      sel.onchange = sortMe;
      ui.vis.grid.me.tbar.sortDir = sel;
    }
    else {  // activities grid
      $$("span", tbarMe, null, null, "&nbsp;");
    }
  }
  */
  
  // (2.2) Prepare "Learners in group" toolbar:
  var tbarOther = null;
  /*
  var tbarOther = $$("div");
  if (doOthers) {
    if (topic === null) {  // topics grid
      // Learner order:
      $$("span", tbarOther, null, null, "Order learners by ");
      var sel = $$("select", tbarOther);
      for (var i=0, ni=data.resources.length; i < ni; i++) {
        $$("option", sel, null, null, data.resources[i].name).value = data.resources[i].id;
      }
      sel.onchange = sortOthers;
      sel.selectedIndex = ui.vis.grid.others.tbar.sortByIdx;
      ui.vis.grid.others.tbar.sortBy = sel;
      
      // Learner order direction:
      var sel = $$("select", tbarOther);
      $$("option", sel, null, null, "Low to high").value = "a";
      $$("option", sel, null, null, "High to low").value = "d";
      sel.selectedIndex = ui.vis.grid.others.tbar.sortDirIdx;
      sel.onchange = sortOthers;
      ui.vis.grid.others.tbar.sortDir = sel;
      
      // Cell height:
      if (state.vis.resIdx >= 0) {
        //$$("span", tbarOther, null, null, " &nbsp;&nbsp;&bull;&nbsp;&nbsp; Block height ");
        $$("span", tbarOther, null, null, "Block height ");
        var sel = $$("select", tbarOther);
        for (var i = CONST.vis.otherIndCellH.min; i <= CONST.vis.otherIndCellH.max; i++) {
          $$("option", sel, null, null, i).value = i;
        }
        sel.selectedIndex = state.vis.otherIndCellH - CONST.vis.otherIndCellH.min;
        sel.onchange = function (e) {
          state.vis.otherIndCellH = parseInt(this.value);
          log(
            "action" + CONST.log.sep02 + "others-cell-height-set"         + CONST.log.sep01 +
            "height" + CONST.log.sep02 + state.vis.otherIndCellH + "px",
            true
          );
          visDo(false, false, true);
        };
        $$("span", tbarOther, null, null, "px");
      }
    }
    else {  // activities grid
      // Cell height:
      if (state.vis.resIdx >= 0) {
        $$("span", tbarOther, null, null, "Block height ");
        var sel = $$("select", tbarOther);
        for (var i = CONST.vis.otherIndCellH.min; i <= CONST.vis.otherIndCellH.max; i++) {
          $$("option", sel, null, null, i).value = i;
        }
        sel.selectedIndex = state.vis.otherIndCellH - CONST.vis.otherIndCellH.min;
        sel.onchange = function (e) {
          state.vis.otherIndCellH = parseInt(this.value);
          visDo(false, false, true);
        };
        $$("span", tbarOther, null, null, "px");
      }
    }
  }
  */
  
  // (2.3) Visualize:
  var fnVisGenGridData = null;
  
  // (2.3.1) All resources:
  if (state.vis.resIdx < 0) {
    fnVisGenGridData = (state.args.uiGridActLstMode || topic === null ? visGenGridDataAllRes : visGenGridDataAllRes_act);
    // @@@@
    switch (state.vis.mode) { // group / individual
      // (2.3.1.1) Group comparison mode:
      case CONST.vis.mode.grp:
        // (a) Me + Me and group + Group:
        if (doMe && state.args.uiGridMeVis) {
          var title = (state.args.uiGridAllHeadMeVis ? "Me" + (topic === null || state.args.uiGridActLstMode ? "" : " &nbsp; <span class=\"info\">(TOPIC: " + topic.name + ")</span>") : null);
          visGenGrid      (ui.vis.grid.cont.me,     fnVisGenGridData(null,     "me",        me,           null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]);                                                    }, data.resources), true,  true ), CONST.vis.gridAbs, title, tbarMe,                       false, true,                        0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barAbsMini, resNames, true,  (topic === null || state.args.uiGridActLstMode ? true : false), true,  true, "all-res-me" );
        }
        
        if ((doMe || doGrp) && state.args.uiGridMeGrpVis) {
          var title = (state.args.uiGridAllHeadMeGrpVis ? "Me versus group" : null);
          visGenGrid      (ui.vis.grid.cont.me,     fnVisGenGridData(null,     "me vs grp", me,           grp,      [],          $map(function (x) { return CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]); }, data.resources), false, true ), CONST.vis.gridDev, title, null,                         false, false,                       0,                           state.vis.grid.cornerRadius, topicMaxW,         state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barDevMini, resNames, true,  false,                                                          false, true , "all-res-mevsgrp" );
        }
        
        if (doGrp && state.args.uiGridGrpVis) {
          var title = (state.args.uiGridAllHeadGrpVis ? "Group" : null);
          visGenGrid      (ui.vis.grid.cont.grp,    fnVisGenGridData(null,     "grp",       grp,          null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp[data.vis.color.binCount - 1]);                                                   }, data.resources), false, true ), CONST.vis.gridAbs, title, null,                         false, false,                       0,                           state.vis.grid.cornerRadius, topicMaxW,         state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barAbsMini, resNames, true,  false,                                                          false, true, "all-res-grp" );
        }
        
        // (b) Others:
        if (doOthers && state.args.uiGridOthersVis) {
          if (state.args.dataReqOtherLearners) {
            for (var i=0, ni=others.length; i < ni; i++) {
              var other = others[i];
              //var othersTitle = "Students in the class";
              
                  
              
              var title = (state.args.uiGridAllHeadOthersVis && i === 0 ? othersTitle + " &nbsp; <span class=\"info\">" + (meIdx === -1 ? "(you are not here)" : "(you are " + (meIdx + 1) + ((meIdx + 1) % 10 === 1 ? "st" : ((meIdx + 1) % 10 === 2 ? "nd" : ((meIdx + 1) % 10 === 3 ? "rd" : "th"))) + " out of " + others.length + ")") + "</span>" : null);
              if (other.id === me.id) {
                colorScales = $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]); }, data.resources);
              }
              else {
                colorScales = $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp[data.vis.color.binCount - 1]); }, data.resources);
              }
              visGenGrid    (ui.vis.grid.cont.others, fnVisGenGridData(null,     "others",    other,        null,     [],          colorScales,                                                                                                                                                         false, true ), CONST.vis.gridAbs, title, (i === 0 ? tbarOther : null), false, (i === 0 && topic === null), CONST.vis.otherIndCellH.def, 0,                           topicMaxW, state.vis.grid.xLblAngle,  0, false, null,      null,                resNames, true,  false,                                                          false, true, null );
            }
          }
          
          //XXX
          //visGenGrid    (ui.vis.grid.cont.others, fnVisGenGridData(null,     "others",    other,        null,     [],          colorScales,                                                                                                                                                         false, true ), CONST.vis.gridAbs, title, (i === 0 ? tbarOther : null), false, (i === 0 && topic === null), CONST.vis.otherIndCellH.def, 0,                           topicMaxW, state.vis.grid.xLblAngle,  0, false, null,      null,                resNames, true,  false,                                                          false, true );
          
          /*
          var title = "Learners in group";
          var gridData = { gridName: "others", topics: $map(function (x) { return x.name }, data.topics), sepX: [], series: [] };
          visGenGrid    (ui.vis.grid.cont.others, gridData, CONST.vis.gridAbs, title, (i === 0 ? tbarOther : null), false, (i === 0 && topic === null), CONST.vis.otherIndCellH.def, 0,                           topicMaxW, state.vis.grid.xLblAngle,  0, false, null,      null,                resNames, true,  false,                                                          false, true );
          */
          
          /*
          data.context.learnerCnt = 17;
          var learnerCntDiff = data.context.learnerCnt - i;
          console.log(i);
          console.log(learnerCntDiff);
          if (learnerCntDiff > 0) {
            $$("div", ui.vis.grid.cont.others, null, null, "<br />" + (learnerCntDiff === 1 ? "One learner is not being shown here because they have not logged in yet." : "" + learnerCntDiff + " learners are not being shown here because they have not logged in yet."));
          }
          */
          $($$input("button", ui.vis.grid.cont.others, "btn-others-load", null, (state.args.dataReqOtherLearners ? "Update learners" : "Load the rest of learners"))).button().click(loadDataOthers);
          /*
  (state.args.reqOtherLearners
    ? $call("GET", CONST.uriServer+"GetContentLevels?usr=" + state.curr.usr + "&grp=" + state.curr.grp + "&sid=" + state.curr.sid + "&cid=" + state.curr.cid + "&mod=" + (state.args.dataReqOtherLearners ? "all" : "all") + "&avgtop=" + state.args.dataTopNGrp, null, loadData_cb, true, false)
  );
  */
        }
        break;
      
      // (2.3.1.1) Individual comparison mode:
      case CONST.vis.mode.ind:
        // (a) My progress:
        if (doMe && state.args.uiGridMeVis) {
          var title = (state.args.uiGridAllHeadMeVis ? "Me" + (topic === null || state.args.uiGridActLstMode ? "" : " &nbsp; <span class=\"info\">(TOPIC: " + topic.name + ")</span>") : null);
          visGenGrid     (ui.vis.grid.cont.me,      fnVisGenGridData(null,     "me",        me,           null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                   }, data.resources), true,  true ), CONST.vis.gridAbs, title, tbarMe,                       false, true,                        0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barAbsMini, resNames, true,  (topic === null || state.args.uiGridActLstMode ? true : false), true,  true, null );
        }
        
        // (b) Others:
        if (doOthers && state.args.uiGridOthersVis) {
          for (var i=0, ni=others.length; i < ni; i++) {
            var other = others[i];
            var title = (state.args.uiGridAllHeadOthersVis && i === 0 ? othersTitle + " &nbsp; <span class=\"info\">" + (meIdx === -1 ? "(you are not here)" : "(you are " + (meIdx + 1) + ((meIdx + 1) % 10 === 1 ? "st" : ((meIdx + 1) % 10 === 2 ? "nd" : ((meIdx + 1) % 10 === 3 ? "rd" : "th"))) + " out of " + others.length + ")") + "</span>" : null);
            visGenGrid    (ui.vis.grid.cont.others, fnVisGenGridData(null,     "others",    other,        null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                   }, data.resources), false, true ), CONST.vis.gridAbs, title, (i === 0 ? tbarOther : null), false, false,                       0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, false, BarChart, CONST.vis.barAbsMini, resNames, true,  false,                                                          false, true, null );
          }
        }

        break;
    }
  }
  
  // (2.3.2) One resource:
  else {
	// @@@@ 
    fnVisGenGridData = (state.args.uiGridActLstMode || topic === null ? visGenGridDataOneRes : (res.id === "AVG" ? visGenGridDataAllRes_act : visGenGridDataOneRes_act));
    
    var res   = data.resources[state.vis.resIdx];  // the currenly selected resource
    var act   = (topic && topic.activities ? topic.activities[res.id] || [] : []);
    
    var topicNames = (topic === null ? $map(function (x) { return x.name; }, data.topics) : [topic.name].concat($map(function (x) { return x.name; }, act)));
    
    switch (state.vis.mode) {
      // (2.3.2.1) Group comparison mode:
      case CONST.vis.mode.grp:
        // (a) My progress, deviation from group, and group:
        if ((doMe || doGrp) && state.args.uiGridMeVis) {
          // Topics and activites in a non-AVG resource-focus:
          if (topic === null || (topic !== null && res.id !== "AVG") || (state.args.uiGridActLstMode)) {
           
            var title = (state.args.uiGridOneHeadMeVis ? (state.args.uiGridGrpVis ? "Me and group" : "My Progress") + (topic === null || state.args.uiGridActLstMode ? "" : " &nbsp; <span class=\"info\">(TOPIC: " + topic.name + ")</span>") : null);
            var seriesNames = ["Me", "Me vs group", "Group"];
            var colorScales = [
              CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]),
              CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]),
              CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1])
            ];
            visGenGrid    (ui.vis.grid.cont.me,     fnVisGenGridData(null,     "me vs grp", me,          grp,       seriesNames, colorScales,                                                                                                                                                         true,  true ), CONST.vis.gridDev, title, tbarMe,                       false, true,                        0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barDevMini, resNames, true,  (topic === null || state.args.uiGridActLstMode ? true : false), true,  true , "one-res" );
          }
          
          // Activites in the AVG resource-focus:
          else {
            if (doMe && state.args.uiGridMeVis) {
                
              var title = (state.args.uiGridAllHeadMeVis ? "Me" + (topic === null || state.args.uiGridActLstMode ? "" : " &nbsp; <span class=\"info\">(TOPIC: " + topic.name + ")</span>") : null);
              visGenGrid  (ui.vis.grid.cont.me,     fnVisGenGridData(null,     "me",        me,           null,     resNames,    $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me[data.vis.color.binCount - 1]);                                                    }, data.resources), true,  true ), CONST.vis.gridAbs, title, tbarMe,                       false, true,                        0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barDevMini, resNames, true,  true,                                                           false, true, null );
            }
            
            if ((doMe || doGrp) && state.args.uiGridMeGrpVis) {
                
              var title = (state.args.uiGridAllHeadMeGrpVis ? "Me versus group" : null);
              visGenGrid  (ui.vis.grid.cont.me,     fnVisGenGridData(null,     "me vs grp", me,           grp,      [],          $map(function (x) { return CONST.vis.colors.grpRev[data.vis.color.binCount - 1].concat(["#eeeeee"], CONST.vis.colors.me[data.vis.color.binCount - 1]); }, data.resources), false, true ), CONST.vis.gridDev, title, null,                         false, false,                       0,                           state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barDevMini, resNames, true,  true,                                                           false, true, null );
            }
            
            if (doGrp && state.args.uiGridGrpVis) {
              
              var title = (state.args.uiGridAllHeadGrpVis ? "Group" : null);
              visGenGrid  (ui.vis.grid.cont.grp,    fnVisGenGridData(null,     "grp",       grp,          null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp[data.vis.color.binCount - 1]);                                                   }, data.resources), false, true ), CONST.vis.gridAbs, title, null,                         false, false,                       0,                           state.vis.grid.cornerRadius, 0,         state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barAbsMini, resNames, true,  true,                                                           false, true, null );
            }
          }
        }
        
        // (b) Others:
 
        if ((doGrp || doOthers) && state.args.uiGridOthersVis) {
            if (state.args.dataReqOtherLearners) {
              // Topics and activites in a non-AVG resource-focus:
              if (topic === null || (topic !== null && res.id !== "AVG")) {
                var gridData = { topics: topicNames, sepX: [1], series: [] };
                for (var i=0, ni=others.length; i < ni; i++) {
                  var other = others[i];
                  var colorScales = (i === meIdx
                    ? colorScales = $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me [data.vis.color.binCount - 1]); }, data.resources)
                    : colorScales = $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp[data.vis.color.binCount - 1]); }, data.resources)
                  );
                  var seriesNames = (meIdx === i ? [(i+1) + ". Me ->"] : [""]);
                  //var seriesNames = [""];
                 fnVisGenGridData(gridData, "others",    other,        null,     seriesNames, colorScales,                                                                                                                                                         false, true );
                }
                var title = (state.args.uiGridOneHeadOthersVis ? othersTitle + " &nbsp; <span class=\"info\">" + (meIdx === -1 ? "(you are not here)" : "(you are " + (meIdx + 1) + ((meIdx + 1) % 10 === 1 ? "st" : ((meIdx + 1) % 10 === 2 ? "nd" : ((meIdx + 1) % 10 === 3 ? "rd" : "th"))) + " out of " + others.length + ")") + "</span>" : null);
                visGenGrid    (ui.vis.grid.cont.others, gridData,                                                                                                                                                                                                                                                        CONST.vis.gridAbs, title, tbarOther,                    false, false,                        state.vis.otherIndCellH,     0,                           topicMaxW, state.vis.grid.xLblAngle,  0, false, null,     null,                 resNames, true,  false,                                                          false, true, null );
              }
              // Activites in the AVG resource-focus:
              else {
                for (var i=0, ni=others.length; i < ni; i++) {
                  var other = others[i];
                  var colorScales = (i === meIdx
                    ? colorScales = $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.me  [data.vis.color.binCount - 1]); }, data.resources)
                    : colorScales = $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.grp [data.vis.color.binCount - 1]); }, data.resources)
                  );
                  var title = (state.args.uiGridOneHeadOthersVis && i === 0 ? othersTitle + " group &nbsp; <span class=\"info\">" + (meIdx === -1 ? "(you are not here)" : "(you are " + (meIdx + 1) + ((meIdx + 1) % 10 === 1 ? "st" : ((meIdx + 1) % 10 === 2 ? "nd" : ((meIdx + 1) % 10 === 3 ? "rd" : "th"))) + " out of " + others.length + ")") + "</span>" : null);
                  visGenGrid  (ui.vis.grid.cont.others, fnVisGenGridData(null,     "others",    other,        null,     resNames,    colorScales,                                                                                                                                                         false, true ), CONST.vis.gridAbs, title, (i === 0 ? tbarOther : null), false, false,                       CONST.vis.otherIndCellH.def, 0,                           topicMaxW, state.vis.grid.xLblAngle,  0, false, null,     null,                resNames, true,   true,                                                           false, true, null );
                }
              }
            }
            $($$input("button", ui.vis.grid.cont.others, "btn-others-load", null, (state.args.dataReqOtherLearners ? "Update learners" : "Load the rest of learners"))).button().click(loadDataOthers);

        }
        break;
      
      // (2.3.2.2) Individual comparison mode:
      case CONST.vis.mode.ind:
        // (a) My progress:
        if (doMe && state.args.uiGridMeVis) {
          // Topics and activites in a non-AVG resource-focus:
          if (topic === null || (topic !== null && res.id !== "AVG")) {
            var title = (state.args.uiGridOneHeadMeVis ? "Me" + (topic === null || state.args.uiGridActLstMode ? "" : " &nbsp; <span class=\"info\">(TOPIC: " + topic.name + ")</span>") : null);
            visGenGrid    (ui.vis.grid.cont.me,     fnVisGenGridData(null,     "me",        me,           null,     [],          [["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1])],                                                                               data.resoueces,  true,  true ), CONST.vis.gridAbs, title, tbarMe,                       false, true,                        0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barAbsMini, resNames, true,  (topic === null || state.args.uiGridActLstMode ? true : false), true,  true, null );
          }
          
          // Activites in the AVG resource-focus:
          else {
            var title = (state.args.uiGridAllHeadMeVis ? "Me" + (topic === null || state.args.uiGridActLstMode ? "" : " &nbsp; <span class=\"info\">(TOPIC: " + topic.name + ")</span>") : null);
            visGenGrid   (ui.vis.grid.cont.me,      fnVisGenGridData(null,     "me",        me,           null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                   }, data.resources), true,  true ), CONST.vis.gridAbs, title, tbarMe,                       false, true,                        0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, true,  BarChart, CONST.vis.barAbsMini, resNames, true,  (topic === null || state.args.uiGridActLstMode ? true : false), true,  true, null );
          }
        }
        
        // (b) Others:
        if (doOthers && state.args.uiGridOthersVis) {
          // Topics and activites in a non-AVG resource-focus:
          if (topic === null || (topic !== null && res.id !== "AVG")) {
            var gridData = { topics: topicNames, sepX: [1], series: [] };
            var idxMe = -1;
            for (var i=0, ni=others.length; i < ni; i++) {
              var other = others[i];
              if (other.id === me.id) idxMe = i;
              var seriesNames = (idxMe === i ? [(i+1) + ". Me"] : [""]);
                                                    fnVisGenGridData(gridData, "other",     other,        null,     seriesNames, $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                   }, data.resources), true,  true );
            }
            var title = (state.args.uiGridOneHeadOthersVis ? othersTitle + " &nbsp; <span class=\"info\">" + (idxMe === -1 ? "(you are not here)" : "(you are " + (idxMe + 1) + ((idxMe + 1) % 10 === 1 ? "st" : ((idxMe + 1) % 10 === 2 ? "nd" : ((idxMe + 1) % 10 === 3 ? "rd" : "th"))) + " out of " + others.length + ")") + "</span>" : null);
            visGenGrid    (ui.vis.grid.cont.others, gridData,                                                                                                                                                                                                                                                        CONST.vis.gridAbs, title, tbarOther,                    false, true,                        state.vis.otherIndCellH,     0,                           topicMaxW, state.vis.grid.xLblAngle,  0, false, null,     null,                 resNames, true,  false,                                                          false, true, null );
          }
          
          // Activites in the AVG resource-focus:
          else {
            for (var i=0, ni=others.length; i < ni; i++) {
              var other = others[i];
              var title = (state.args.uiGridAllHeadOthersVis && i === 0 ? othersTitle + " &nbsp; <span class=\"info\">" + (meIdx === -1 ? "(you are not here)" : "(you are " + (meIdx + 1) + ((meIdx + 1) % 10 === 1 ? "st" : ((meIdx + 1) % 10 === 2 ? "nd" : ((meIdx + 1) % 10 === 3 ? "rd" : "th"))) + " out of " + others.length + ")") + "</span>" : null);
              visGenGrid  (ui.vis.grid.cont.others,fnVisGenGridData(null,     "others",    other,        null,     [],          $map(function (x) { return ["#eeeeee"].concat(CONST.vis.colors.indiv[data.vis.color.binCount - 1]);                                                   }, data.resources),  false, true ), CONST.vis.gridAbs, title, (i === 0 ? tbarOther : null), false, false,                       0,                           state.vis.grid.cornerRadius, topicMaxW, state.vis.grid.xLblAngle, 30, false, BarChart, CONST.vis.barAbsMini, resNames, true,  false,                                                          false, true, null );
            }
          }
        }
        break;
    }
  }
  
  // (3) Sunburst:
  $removeChildren(ui.vis.sunburst);
  
  // (4) Other:
  window.scrollTo(scroll.x, scroll.y);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Generated data for the grid visualization based on all resources.  Separate grid data should be 
 * generated for the current learner, the group, and the deviation from the group.
 * 
 * If 'gridData' is null a new object is returned.  Otherwise, the one passed is modified.
 * 
 * If 'learner02' is defined then the difference between them and the first learner is returned.  This 
 * is utilized in the deviation from group calulations where the second learner is the group.
 */
function visGenGridDataAllRes(gridData, gridName, learner01, learner02, seriesNames, colorScales, doShowSeq, doIncAvg) {
  if (gridData === null || gridData === undefined) var gridData = { gridName: gridName, topics: $map(function (x) { return x.name; }, data.topics), sepX: (doIncAvg ? [1] : []), series: [] };
  
  for (var i=0, ni=data.resources.length; i < ni; i++) {
    var r = data.resources[i];
    var s;
    if(i==0){ // the first has id as it is used for display the help button
        s = { id: "h", resIdx: i, name: r.name, colorScale: colorScales[i], doShowSeq: doShowSeq, data: [] };  // new series
    }else{
        s = { resIdx: i, name: r.name, colorScale: colorScales[i], doShowSeq: doShowSeq, data: [] };  // new series
    }
    
    for (var j=(doIncAvg ? 0 : 1), nj=data.topics.length; j < nj; j++) {
      var t = data.topics[j];
      s.data.push({
        topicIdx : j,
        resIdx   : i,
        actIdx   : -1,
        //seq      : (t.sequencing !== undefined ? t.sequencing[r.id] || 0 : 0),
        seq      : (doShowSeq && learner01.state.topics[t.id].sequencing !== undefined ? learner01.state.topics[t.id].sequencing[r.id] || 0 : 0),
        val      : learner01.state.topics[t.id].values[r.id][getRepLvl().id] - (learner02 === null ? 0 : learner02.state.topics[t.id].values[r.id][getRepLvl().id]),
        valMe    : learner01.state.topics[t.id].values[r.id][getRepLvl().id],
        valGrp   : (learner02 === null ? -1 : learner02.state.topics[t.id].values[r.id][getRepLvl().id]),
        isInt    : (learner01.id === data.context.learnerId && r.id !== "AVG"),
        isVis    : true
      });
    }
    gridData.series.push(s);
  }
  
  return gridData;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Generated data for the grid visualization based on only the currently selected resource.  The 
 * resulting grid data combines data for the current learner, the group, and the deviation from the 
 * group.  Therefore, separate grids are unnecessary.
 * 
 * If 'gridData' is null a new object is returned.  Otherwise, the one passed is modified.
 * 
 * If 'learner02' is defined then the difference between them and the first learner is returned.  This 
 * is utilized in the deviation from group calulations where the second learner is the group.
 * 
 * 'seriesNames' is an array which redefines the default series names if the appropriate elements are 
 * provided.  If no name redefinition is desired, the array should be empty instead of null.
 */
function visGenGridDataOneRes(gridData, gridName, learner01, learner02, seriesNames, colorScales, doShowSeq, doIncAvg) {
  if (gridData === null || gridData === undefined) var gridData = { gridName: gridName, topics: $map(function (x) { return x.name; }, data.topics), sepX: (doIncAvg ? [1] : []), series: [] };
  // @@@@

  var r = data.resources[state.vis.resIdx];  // the currenly selected resource
  var s = null;
  
  // Me:
  s = { id: "me-h", resIdx: state.vis.resIdx, name: (seriesNames[0] !== undefined ? seriesNames[0] : "Me"), colorScale: colorScales[0], doShowSeq: doShowSeq, data: [] };
  
  for (var j=(doIncAvg ? 0 : 1), nj=data.topics.length; j < nj; j++) {
    var t = data.topics[j];
    // if the resource is the average (overall) with index 0, then fill sequencing as the aggregation of the sequencing of individual resources
    if (state.vis.resIdx == 0 && learner01.state.topics[t.id].sequencing !== undefined){
    	var seq = 0;
    	
    	for (var iRes=1, nRes=data.resources.length; iRes < nRes; iRes++) {
    	    if (learner01.state.topics[t.id].sequencing[data.resources[iRes].id] > seq) seq=learner01.state.topics[t.id].sequencing[data.resources[iRes].id];
        }
    	learner01.state.topics[t.id].sequencing[r.id] = seq;
    }

    s.data.push({
      topicIdx : j,
      resIdx   : state.vis.resIdx,
      actIdx   : -1,
      //seq      : (t.sequencing !== undefined ? t.sequencing[r.id] || 0 : 0),
      seq      : (doShowSeq && learner01.state.topics[t.id].sequencing !== undefined ? learner01.state.topics[t.id].sequencing[r.id] || 0 : 0),
      val      : learner01.state.topics[t.id].values[r.id][getRepLvl().id],
      valMe    : learner01.state.topics[t.id].values[r.id][getRepLvl().id],
      valGrp   : -1,
      isInt    : (r.id !== "AVG"),
      isVis    : true
    });
  }
  
  gridData.series.push(s);
  
  // Me versus group:
  if (learner02 !== null && state.args.uiGridMeGrpVis) {
    s = { id: "mevsgrp-h", resIdx: state.vis.resIdx, name: (seriesNames[1] !== undefined ? seriesNames[1] : "Me vs. group"), colorScale: colorScales[1], doShowSeq: false, data: [] };
    
    for (var j=(doIncAvg ? 0 : 1), nj=data.topics.length; j < nj; j++) {
      var t = data.topics[j];
      s.data.push({
        topicIdx : j,
        resIdx   : state.vis.resIdx,
        actIdx   : -1,
        seq      : 0,
        val      : learner01.state.topics[t.id].values[r.id][getRepLvl().id] - (learner02 === null ? 0 : learner02.state.topics[t.id].values[r.id][getRepLvl().id]),
        valMe    : learner01.state.topics[t.id].values[r.id][getRepLvl().id],
        valGrp   : (learner02 === null ? -1 : learner02.state.topics[t.id].values[r.id][getRepLvl().id]),
        isInt    : false,
        isVis    : true
      });
    }
    
    gridData.series.push(s);
  }
  
  // Group:
  if (learner02 !== null && state.args.uiGridGrpVis) {
    s = { id: "grp-h", resIdx: state.vis.resIdx, name: (seriesNames[2] !== undefined ? seriesNames[2] : "Group"), colorScale: colorScales[2], doShowSeq: false, data: [] };
    
    for (var j=(doIncAvg ? 0 : 1), nj=data.topics.length; j < nj; j++) {
      var t = data.topics[j];
      s.data.push({
        topicIdx : j,
        resIdx   : state.vis.resIdx,
        actIdx   : -1,
        seq      : 0,
        val      : -learner02.state.topics[t.id].values[r.id][getRepLvl().id],
        valMe    : -1,
        valGrp   : learner02.state.topics[t.id].values[r.id][getRepLvl().id],
        isInt    : false,
        isVis    : true
      });
    }
    
    gridData.series.push(s);
  }
  
  return gridData;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Return grid data for activities when in all resources mode (see desription for topics above for more 
 * info).
 */
function visGenGridDataAllRes_act(gridData, gridName, learner01, learner02, seriesNames, colorScales, doShowSeq, doIncAvg) {
  var topic = getTopic();
  // (1) Determing max number of columns:
  var colCntMax = -1;
  for (var i=0, ni=data.resources.length; i < ni; i++) {
    var res = data.resources[i];
    var act = topic.activities[res.id];
    colCntMax = Math.max(colCntMax, (act ? act.length : 0));
  }
  
  // (2) Create the gridData object if necessary:
  if (gridData === null || gridData === undefined) {
    var gridData = { gridName: gridName, topics: /*[topic.name]*/(doIncAvg ? (state.args.uiGridActLstMode ? ["OVERALL"] : ["BACK TO TOPICS"]) : []), sepX: (doIncAvg ? [1] : []), series: [] };
    for (var i = 0; i < colCntMax; i++) gridData.topics.push("");
  }
  
  // (3) Generate data:
  for (var i=(state.args.uiGridActLstMode ? 1 : 0), ni=data.resources.length; i < ni; i++) {
    var res = data.resources[i];
    var act = topic.activities[res.id];
    
    // (3.1) Prepare series:
    var s = { resIdx: i, name: res.name, colorScale: colorScales[i], doShowSeq: doShowSeq, data: [] };  // new series
    
    // (3.2) Add the topic (which serves as the average over all activities):
    if (doIncAvg) {
      s.data.push({
        topicIdx : state.vis.topicIdx,
        resIdx   : i,
        actIdx   : -1,
        seq      : 0,
        val      : learner01.state.topics[topic.id].values[res.id][getRepLvl().id] - (learner02 === null || !learner01.state.topics ? 0 : learner02.state.topics[topic.id].values[res.id][getRepLvl().id]),
        valMe    : learner01.state.topics[topic.id].values[res.id][getRepLvl().id],
        valeGrp  : (learner02 === null || !learner01.state.topics ? -1 : learner02.state.topics[topic.id].values[res.id][getRepLvl().id]),
        isInt    : true,
        isVis    : true
      });
    }
    
    // (3.3) Add activities:
    var colCnt = 0;
    if (act && learner01.state.activities) {
      for (var j=0, nj=act.length; j < nj; j++) {
        var a = act[j];
        
        s.data.push({
          topicIdx : state.vis.topicIdx,
          resIdx   : i,
          actIdx   : j,
          actName  : a.name,
          //seq      : (a.sequencing || 0),
          //seq      : (doShowSeq && learner01.state.activities[topic.id][res.id][a.id].sequencing !== undefined ? learner01.state.activities[topic.id][res.id][a.id].sequencing || 0 : 0),
          seq      : (doShowSeq && learner01.state.activities[topic.id][data.resources[i].id][a.id].sequencing !== undefined ? learner01.state.activities[topic.id][data.resources[i].id][a.id].sequencing || 0 : 0),
          val      : learner01.state.activities[topic.id][res.id][a.id].values[getRepLvl().id] - (learner02 === null || !learner01.state.activities ? 0 : learner02.state.activities[topic.id][res.id][a.id].values[getRepLvl().id]),
          valMe    : learner01.state.activities[topic.id][res.id][a.id].values[getRepLvl().id],
          valGrp   : (learner02 === null || !learner01.state.activities ? -1 : learner02.state.activities[topic.id][res.id][a.id].values[getRepLvl().id]),
          isInt    : true,
          isVis    : true
        });
        colCnt++;
      }
    }
    
    // Add empty data points to make all series equal length:
    for (var j = colCnt; j < colCntMax; j++) {
      s.data.push({ resIdx: i, topicIdx: state.vis.topicIdx, actIdx: -1, seq: 0, val: 0, isInt: false, isVis: false });
    }
    
    gridData.series.push(s);
  }
  
  return gridData;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Return grid data for activities when in one resource mode (see desription for topics above for more 
 * info).
 */
function visGenGridDataOneRes_act(gridData, gridName, learner01, learner02, seriesNames, colorScales, doShowSeq, doIncAvg) {
  var topic = getTopic();
  var res   = data.resources[state.vis.resIdx];  // the currenly selected resource
  var act   = (topic.activities ? topic.activities[res.id] || [] : []);
  
  // (1) Determing max number of columns:
  var colCntMax = (act ? act.length : 0);
  
  // (2) Create the gridData object if necessary:
  if (gridData === null || gridData === undefined) {
    var gridData = { gridName: gridName, topics: /*[topic.name]*/(doIncAvg ? (state.args.uiGridActLstMode ? ["OVERALL"] : ["BACK TO TOPICS"]) : []).concat($map(function (x) { return x.name; }, act)), sepX: (doIncAvg ? [1] : []), series: [] };
    for (var i = 0; i < colCntMax; i++) gridData.topics.push("");
  }
  
  var s      = null;  // a series
  var colCnt = 0;
  
  // (3) Generate data:
  // (3.1) Me:
  s = { resIdx: state.vis.resIdx, name: seriesNames[0], colorScale: colorScales[0], doShowSeq: doShowSeq, data: [] };
  
  // (3.1.1) Add the topic (which serves as the average over all activities):
  if (doIncAvg) {
    s.data.push({
      topicIdx : state.vis.topicIdx,
      resIdx   : state.vis.resIdx,
      actIdx   : -1,
      seq      : 0,
      val      : learner01.state.topics[topic.id].values[res.id][getRepLvl().id],
      valMe    : learner01.state.topics[topic.id].values[res.id][getRepLvl().id],
      valGrp   : -1,
      isInt    : true,
      isVis    : true
    });
  }
  
  // (3.1.2) Add the activities:
  colCnt = 0;
  if (act && learner01.state.activities) {
    for (var j=0, nj=act.length; j < nj; j++) {
      var a = act[j];
      s.data.push({
        topicIdx : state.vis.topicIdx,
        resIdx   : state.vis.resIdx,
        actIdx   : j,
        actName  : a.name,
        //seq      : (a.sequencing || 0),
        seq      : (doShowSeq && learner01.state.activities[topic.id][res.id][a.id].sequencing !== undefined ? learner01.state.activities[topic.id][res.id][a.id].sequencing || 0 : 0),
        val      : learner01.state.activities[topic.id][res.id][a.id].values[getRepLvl().id],
        valMe    : learner01.state.activities[topic.id][res.id][a.id].values[getRepLvl().id],
        valeGrp  : -1,
        isInt    : true,
        isVis    : true
      });
      colCnt++;
    }
  }
  
  gridData.series.push(s);
  
  // (3.2) Me versus group:
  if (learner02 !== null) {
    s = { resIdx: state.vis.resIdx, name: seriesNames[1], colorScale: colorScales[1], doShowSeq: false, data: [] };
    
    // (3.2.1) Add the topic (which serves as the average over all activities):
    if (doIncAvg) {
      s.data.push({
        topicIdx : state.vis.topicIdx,
        resIdx   : state.vis.resIdx,
        actIdx   : -1,
        seq      : 0,
        val      : learner01.state.topics[topic.id].values[res.id][getRepLvl().id],
        valMe    : learner01.state.topics[topic.id].values[res.id][getRepLvl().id],
        valGrp   : -1,
        isInt    : true,
        isVis    : true
      });
    }
    
    // (3.2.2) Add the activities:
    colCnt = 0;
    if (act && learner01.state.activities) {
      for (var j=0, nj=act.length; j < nj; j++) {
        var a = act[j];
        s.data.push({
          topicIdx : state.vis.topicIdx,
          resIdx   : state.vis.resIdx,
          actIdx   : j,
          actName  : a.name,
          seq      : 0,
          val      : learner01.state.activities[topic.id][res.id][a.id].values[getRepLvl().id] - (learner02 === null || !learner01.state.activities ? 0 : learner02.state.activities[topic.id][res.id][a.id].values[getRepLvl().id]),
          valMe    : learner01.state.activities[topic.id][res.id][a.id].values[getRepLvl().id],
          valGrp   : (learner02 === null || !learner01.state.activities ? 0 : learner02.state.activities[topic.id][res.id][a.id].values[getRepLvl().id]),
          isInt    : false,
          isVis    : true
        });
        colCnt++;
      }
    }
    
    gridData.series.push(s);
  }
  
  // (3.3) Group:
  if (learner02 !== null) {
    s = { resIdx: state.vis.resIdx, name: seriesNames[2], colorScale: colorScales[2], doShowSeq: false, data: [] };
    
    // (3.2.1) Add the topic (which serves as the average over all activities):
    if (doIncAvg) {
      s.data.push({
        topicIdx : state.vis.topicIdx,
        resIdx   : state.vis.resIdx,
        actIdx   : -1,
        seq      : 0,
        val      : -learner02.state.topics[topic.id].values[res.id][getRepLvl().id],
        valMe    : -1,
        valGrp   : learner02.state.topics[topic.id].values[res.id][getRepLvl().id],
        isInt    : true,
        isVis    : true
      });
    }
    
    // (3.3.2) Add the activities:
    colCnt = 0;
    if (act && learner01.state.activities) {
      for (var j=0, nj=act.length; j < nj; j++) {
        var a = act[j];
        s.data.push({
          topicIdx : state.vis.topicIdx,
          resIdx   : state.vis.resIdx,
          actIdx   : j,
          actName  : a.name,
          seq      : 0,
          val      : -learner02.state.activities[topic.id][res.id][a.id].values[getRepLvl().id],
          valMe    : -1,
          valGrp   : learner02.state.activities[topic.id][res.id][a.id].values[getRepLvl().id],
          isInt    : false,
          isVis    : true
        });
        colCnt++;
      }
    }
    
    gridData.series.push(s);
  }
  
  return gridData;
}


// ------------------------------------------------------------------------------------------------------
function visGenSunburstData(topic, learner01, learner02, colorScale) {
  return { topic: topic, colorScale: colorScale };
}


// ------------------------------------------------------------------------------------------------------
/**
 * Should the cell width be varied (according to the selected topic or activity variable)?
 */
function visDoVaryCellW() {
  return (state.vis.topicSize.attr.length > 0 && getTopic() === null);
}


// ------------------------------------------------------------------------------------------------------
/**
 * Generates a grid.
 */
function visGenGrid(cont, gridData, settings, title, tbar, doShowYAxis, doShowXLabels, sqHFixed, cornerRadius, topicMaxW, xLblAngle, extraPaddingB, isInteractive, miniVis, miniSettings, resNames, doShowResNames, doShowTimeline, doReserveTimelineSpace, doUpdActLstTopicCellX, helpId) {
  var tbl = $$tbl(cont, null, "grid", 0, 0);
  if (doUpdActLstTopicCellX) ui.vis.actLst.topicCellX = [];
  
  // (1) Header:
  // Title:
  if (title !== null && title.length > 0) {
    $setAttr($$("td", $$("tr", tbl), null, "title", title), { colspan: 2 });
  }
  
  // Toolbar:
  if (tbar !== null) {
    var td = $setAttr($$("td", $$("tr", tbl), null, "tbar"), { colspan: 2 });
    td.appendChild(tbar);
  }
  
  // (2) Generate visualization:
  // (2.1) Calculate some important values:
  doShowTimeline = doShowTimeline && state.args.uiGridTimelineVis;
  
  var topicOffsetT = svgGetMaxTextBB([title]).height + 4;
  //var resOffsetL = svgGetMaxTextBB($.map(gridData.series, function (x) { return x.name; })).width + 10;
  var resOffsetL = svgGetMaxTextBB(resNames).width + 10;
  var topicMaxWCos = Math.ceil(topicMaxW * Math.cos((xLblAngle === 45 ? 45 : 0) * (Math.PI / 180)));
  var paddingL = (doShowYAxis ? settings.padding.l : 10);
  var paddingT = (doShowXLabels ? topicMaxWCos : 0);
  //var sqW = Math.floor((settings.w - paddingL - settings.padding.r - settings.sq.padding) / gridData.series[0].data.length);
  var sqW = settings.sq.w;
  var sqH = (sqHFixed === 0 ? sqW : sqHFixed);
  var visW = ((sqW + settings.sq.padding) * gridData.series[0].data.length) + paddingL + settings.padding.r + resOffsetL;
  var visH = ((sqH + settings.sq.padding) * gridData.series.length) + settings.padding.t + settings.padding.b + topicOffsetT + paddingT + (doReserveTimelineSpace && state.args.uiGridTimelineVis ? 30 : 0);
  var sepXAggr = 0;
  
  if (visDoVaryCellW()) {
    var topicSizeSum = $lfold(function (a,b) { return a+b; }, $map(function (x) { return visGetTopicSize(x); }, data.topics), 0);
    sqW = Math.floor(sqW / (topicSizeSum / gridData.series[0].data.length));  // in the case of equal topic sizes, the denominator is 1 and therefore wouldn't change the value of sqW, but for unequal topic sizes it scales the default sqW
  }
  
  CONST.vis.otherIndCellH.max = sqW;
  
  var tr = $$("tr", tbl);
  
  // (2.3) Prepare scales:
  var scaleX =
    d3.scale.ordinal().
    domain(gridData.topics).
    rangePoints([ paddingL + sqW / 2 + resOffsetL, visW - settings.padding.r - sqW / 2 ]);
  
  var scaleY = $map(
    function (x) {
      var scale =
        d3.scale.linear().
        domain(settings.scales.y).
        range(x.colorScale);
      return scale;
    },
    gridData.series
  );
  
  // (2.4) Prepare axes:
  // (nothing to do here at this point because no axes are shown)
  
  // (2.5) SVG:
  var svg =
    d3.select($$("td", tr)).
    append("svg").
    attr("style", "padding-bottom: " + (gridData.series.length > 1 ? extraPaddingB : 0) + "px;").
    attr("width", visW + (gridData.sepX.length * settings.sepX) + (xLblAngle === 45 ? topicMaxWCos : 0)).
    attr("height", visH);
  
  // (2.6) Mini bar chart series:
  var mini = { svg: null, settings: miniSettings, series: {} };
  if (miniVis) {
    mini.svg = miniVis($$("td", tr), gridData, mini.settings, null, 2, false).
      //addSeries("pri", { sepX: gridData.sepX, series: $.map(data.series, function (x) { return 0; }) }, 0, "l-gray", null, null).
      addSeries("pri", gridData, 0, 0, "l-gray", function (x) { return x.val; }, null).
      setVis(false).
      style("margin-top", (topicOffsetT + paddingT - mini.settings.padding.t) + "px");
  }
  else {
    $$("td", tr);  // preserve the two-column table layout for consistency
  }
  
  // (2.7) X axis:
  if (doShowXLabels) {
    var txtX = (!visDoVaryCellW() ? (sqW / 2 - 2) : 6);  // the x-coordinate of the text label being drawn
    svg.
      append("g").
      attr("class", "x-axis").
      selectAll("text").
      data(gridData.topics).
      enter().
        append("text").
        attr("x", 1).
        attr("y", 1).
        style("text-anchor", "start").
        text(function (d) { return d; }).
        attr("transform", function (d,i) {
          if ($.inArray(i, gridData.sepX) !== -1) { txtX += settings.sepX; }
          txtX += (i === 0 ? 0 : sqW * visGetTopicSize(data.topics[i-1]) + settings.sq.padding);
          return "translate(" + (resOffsetL + paddingL + txtX + 1) + "," + (topicOffsetT + paddingT) + ") rotate(-45)";
        }).
        style("text-rendering", "geometricPrecision");
  }
  
  // (2.8) The grid:
  var gGrid = svg.
    append("g").
    attr("class", "grid");
  
  for (var iSeries = 0; iSeries < gridData.series.length; iSeries++) {
    var s = gridData.series[iSeries];
    var res = data.resources[s.resIdx];
    
    // Resource name:
    if (doShowResNames) {
      svg.
        append("text").
        attr("x", 1).
        attr("y", ((sqH + settings.sq.padding) * iSeries) + (sqH / 2) + 5 + topicOffsetT + paddingT).
        text(s.name).
        attr("class", "res").
        style("text-rendering", "geometricPrecision");
    }
    
    // Help:
    //
    if(state.args.uiShowHelp && helpId && s.id){
      svg.
        append("g").
        attr("class", "helpButton").
        //attr("style","background-image: url('img/help.gif');").
        attr("helpId",helpId).
        attr("serieId",(s.id ? s.id : "")).
        attr("cursor","pointer").
        on("click",function (e) {
            var origin = d3.select(this).attr("helpId") + '-' + d3.select(this).attr("serieId");
            helpDialogShow(origin,event.pageX,event.pageY);
        }).
        on("mouseover",function () {d3.select(this).style("opacity","1");}).
        on("mouseout",function () {d3.select(this).style("opacity","0.7");}).
        style("opacity", "0.7").
          append("image").
          attr("x", (resOffsetL + paddingL + settings.sepX + (sqW+settings.sq.padding) * data.topics.length + 10)).
          attr("y", ((sqH + settings.sq.padding) * iSeries)  + 5 + topicOffsetT + paddingT).
          attr("width", 22).
          attr("height", 19).
          attr("xlink:href","img/help.png");

    }
    
    // Mini-series (e.g., bar chart):
    if (miniVis) {
      mini.series[res.id] = [];
      for (var j=0, nj=gridData.series[0].data.length; j < nj; j++) {
        mini.series[res.id].push(s.data[j].val);
      }
    }
    
    // Grid cells -- The group:
    var sqX = 0;  // the x-coordinate of the cell being drawn
    
    var g = gGrid.
      selectAll("grid-" + res.id).
      data(s.data).
        enter().
        append("g").
        attr("class", "grid-cell-outter").
        attr("transform", function (d,i) {
          if ($.inArray(i, gridData.sepX) !== -1) { sqX += settings.sepX; }
          sqX += (i === 0 ? 0 : sqW * visGetTopicSize(data.topics[i-1]) + settings.sq.padding);
          var x = resOffsetL + paddingL + sqX;
          var y = ((sqH + settings.sq.padding) * iSeries) + settings.padding.t + topicOffsetT + paddingT;
          
          if (doUpdActLstTopicCellX && iSeries === 0 && i > 0) ui.vis.actLst.topicCellX.push(x + (sqW / 2));  // save the x-coordinate of cell to align activities list
          
          return "translate(" + x + "," + y + ")";
        }).
        
        attr("data-grid-name",  gridData.gridName).
        attr("data-idx",        function (d,i) { return i; }).
        attr("data-series-idx", iSeries).
        attr("data-var-id",     res.id).
        attr("data-var-name",   res.name).
        attr("data-topic-idx",  function (d) { return d.topicIdx; }).
        attr("data-val",        function (d) { return d.val; }).
        attr("data-val-me",     function (d) { return d.valMe  != null ? d.valMe  : -1; }).
        attr("data-val-grp",    function (d) { return d.valGrp != null ? d.valGrp : -1; }).
        attr("data-res-idx",    function (d) { return d.resIdx; }).
        attr("data-act-idx",    function (d) { return d.actIdx; }).
        attr("data-cell-idx",   function (d) { return state.vis.grid.cellIdxMax++; }).
        
        append("g").
        attr("class", "grid-cell-inner");
    
    // Grid cells -- The main element (the square):
    g.
      append("rect").
      attr("class", "box").
      attr("x", 0).
      attr("y", 0).
      attr("width", function (d,i) { return (d.isVis ? sqW * visGetTopicSize(data.topics[i]) : 0); }).
      attr("height", function (d) { return (d.isVis ? sqH : 0); }).
      attr("rx", (!visDoVaryCellW() ? cornerRadius : 0)).
      attr("ry", (!visDoVaryCellW() ? cornerRadius : 0)).
      attr("style", function (d) { var d2 = (d.val >=0 ? data.vis.color.value2color(d.val) : -data.vis.color.value2color(-d.val)); return "fill: " + scaleY[iSeries](d2) + ";"; }).
      style("shape-rendering", "geometricPrecision").
      append("title").
      text(function (d) {
          var tooltip = "";
          if (d.actName != null) tooltip += d.actName + '\n';
          if ( d.valMe != -1  ) {
              tooltip  += getRepLvl().name +' : '+ parseFloat(Math.round(Math.min(d.valMe,1) * 100)).toFixed(0)+'%';
              if( !isNaN(d.valGrp) && d.valGrp != -1 )  tooltip += '\n';
          }
          if ( !isNaN(d.valGrp) && d.valGrp != -1 ) tooltip += 'Group ' + getRepLvl().name +' : '+ parseFloat(Math.round(Math.min(d.valGrp,1) * 100)).toFixed(0)+'%';
          return tooltip; 
      });
    
    // Grid cells -- Sequencing:
    if (s.doShowSeq) {
        if(CONST.vis.seqStars){
            g
            .append("svg:polygon")
            .attr("id", "star_1")
            .attr("visibility", "visible")
            //.attr("points", CalculateStarPoints(6, 6, function (d) { return (d.seq === 0 ? 0 : 5); }, 10, 5))
            .attr("points", function (d) {  return (d.seq === 0 ? "0,0" : CalculateStarPoints(6, 6, 5, Math.max((2+Math.round(8*(d.seq-0.50)/0.5)),4), Math.max((2+Math.round(8*(d.seq-0.50)/0.5))/2,2))); })
            .attr("style", function (d) { return "fill: " + CONST.vis.colors.sequencing + ";"; })
            //.attr("style", function (d) { return "border: 1px solid #FFFFFF;"; })
            .attr("stroke", "white")
            .style("shape-rendering", "geometricPrecision");
        }else{
            g.
            append("circle").
            attr("class", "seq").
            attr("cx", 6).
            attr("cy", 6).
            //attr("r", function (d) { return (d.seq === 0 ? 0 : Math.max(d.seq * 4, 1)); }).
            attr("r", function (d) { return (d.seq === 0 ? 0 : 4); }).
            attr("stroke", "white").

            // append("path").
            // attr("class", "seq").
            // attr("d", function (d,i) { return (i > 0 && Math.random() <= 0.10 ? "M0,8 v-6 l2,-2 h6 z" : "M0,0"); }).
            attr("style", function (d) { return "fill: " + CONST.vis.colors.sequencing + ";"; }).
            //attr("style", function (d) { return "fill: #000000;" }).
            style("shape-rendering", "geometricPrecision"); 
        }
 
    }
    
    //g.on("mouseover", function (d,i) { console.log(d); })
  }
  
  // (2.9) Timeline:
  if (doShowTimeline) {
    var gTimeline = svg.
      append("g").
      attr("class", "timeline").
      attr("transform", "translate(" + (resOffsetL + paddingL) + "," + (visH - 20) + ")");
    
    // (2.9.1) Line:
    gTimeline.
      append("line").
      attr("x1", (!visDoVaryCellW() ? (sqW / 2 - 2) : 6) + (sqW + settings.sq.padding) + (sqW / 2)).
      attr("y1", 0).
      attr("x2", (!visDoVaryCellW() ? (sqW / 2 - 2) : 6) + (sqW + settings.sq.padding) * (gridData.series[0].data.length - 1) + (gridData.sepX.length * settings.sepX)).
      attr("y2", 0).
      style("shape-rendering", "geometricPrecision");
    
    // (2.9.2) Points:
    var circleX = (!visDoVaryCellW() ? (sqW / 2 - 2) : 6);  // the x-coordinate of the timeline circle
    
    gTimeline.
      selectAll("circle").
      data(gridData.topics).
        enter().
        append("circle").
        attr("class", function (d,i) {
          if(data.topics[i].timeline){
             return  (data.topics[i].timeline.current ? "current" : (data.topics[i].timeline.covered ? "covered" : ""));
          }
          //if (i <=  14) return "covered";
          //if (i === 15) return "current";
          return "";
        }).
        attr("cx", function (d,i) {
          if(data.topics[i].timeline){
              return  (data.topics[i].timeline.current ? 3 : 2);
          }
          //if (i <=  14) return 2;
          //if (i === 15) return 3;
          return 2;
        }).
        attr("cy", 0).
        attr("r", function (d,i) {
          if (i ===  0) return 0;
          if(data.topics[i].timeline){
              return  (data.topics[i].timeline.current ? 6 : 3);
          }  
          
          //if (i <=  14) return 4;
          //if (i === 15) return 8;
          //if (i === 15) return 0;
          return 4;
        }).
        attr("transform", function (d,i) {
          if ($.inArray(i, gridData.sepX) !== -1) { circleX += settings.sepX; }
          circleX += (i === 0 ? 0 : sqW * visGetTopicSize(data.topics[i-1]) + settings.sq.padding);
          return "translate(" + (circleX) + ",0)";
        }).
        style("shape-rendering", "geometricPrecision");
    
    /*
    var circleX = (!visDoVaryCellW() ? (sqW / 2 - 2) : 6);  // the x-coordinate of the timeline circle
    gTimeline.
      selectAll("path").
      data(gridData.topics).
        enter().
        append("path").
        attr("class", function (d,i) {
          if (i <=  8) return "covered";
          if (i === 9) return "current";
          return "";
        }).
        attr("d", function (d,i) {
          if (i === 9) return "M-9,6 L0,-9 L9,6 z";
          return "M0,0";
        }).
        attr("transform", function (d,i) {
          if ($.inArray(i, gridData.sepX) !== -1) { circleX += settings.sepX; }
          circleX += (i === 0 ? 0 : sqW * visGetTopicSize(data.topics[i-1]) + settings.sq.padding);
          return "translate(" + (circleX) + ",0)";
        }).
        style("shape-rendering", "geometricPrecision");
    */
    
    var txtX = (!visDoVaryCellW() ? (sqW / 2) : 6);  // the x-coordinate of the text label being drawn
    gTimeline.
      selectAll("text").
      data(gridData.topics).
        enter().
        append("text").
        attr("class", function (d,i) {
          if (i ===  0) return "header";
          if(data.topics[i].timeline){
              return  (data.topics[i].timeline.current ? "current" : (data.topics[i].timeline.covered ? "covered" : ""));
          }
          //if (i <=  14) return "covered";
          //if (i === 15) return "current";
          return "";
        }).
        attr("x", function (d,i) { return (i === 9 ? 1 : 0); }).
        attr("y", 0).
        style("text-anchor", "middle").
        text(function (d,i) {
          if (i === 0) return state.args.uiGridTimelineTitle;
          return i;
        }).
        attr("transform", function (d,i) {
          if ($.inArray(i, gridData.sepX) !== -1) { txtX += settings.sepX; }
          txtX += (i === 0 ? 0 : sqW * visGetTopicSize(data.topics[i-1]) + settings.sq.padding);
          return "translate(" + (txtX) + ",20)";
        }).
        style("text-rendering", "geometricPrecision");
  }
  
  // (2.10) Events:
  if (isInteractive && miniVis) {
    svg.
      on("click", null).
      on("mouseover",
        function (miniSvg) {
          return function (e) {ehVisGridMouseOver(e, d3.select(this), miniSvg);};
        }(mini.svg)
      ).
      on("mouseout",
        function (miniSvg) {
          return function (e) {
            ehVisGridMouseOut(e, d3.select(this), miniSvg);
          };
        }(mini.svg)
      );
    
  }
  
  if (isInteractive) {
    if (!miniVis) {
      gGrid.
        selectAll(".grid-cell-outter").
        on("mouseover", function (e) { ehVisGridBoxMouseOver(e, d3.select(this), gridData, null, null); }).
        on("mouseout", function (e) { ehVisGridBoxMouseOut(e, d3.select(this), null); }).
        on("click", function (e) { ehVisGridBoxClick(e, d3.select(this)); });
    }
    else {
      gGrid.
        selectAll(".grid-cell-outter").
        on("mouseover",
          function (gridData, miniSvg, miniSeries) {
            return function (e) {
              ehVisGridBoxMouseOver(e, d3.select(this), gridData, miniSvg, miniSeries);
            };
          }(gridData, mini.svg, mini.series)
        ).
        on("mouseout",
          function (miniSvg) {
            return function (e) {
              ehVisGridBoxMouseOut(e, d3.select(this), miniSvg);
            };
          }(mini.svg)
        ).
        on("click", function (e) { ehVisGridBoxClick(e, d3.select(this)); });
    }
    
  }
  
  return svg;
}


// ------------------------------------------------------------------------------------------------------
/**
 * Returns the width size of the grid cell being a proportion of the height, i.e., <0,1>.  If the width 
 * turns out to be smaller than the minimum that minimum is returned instead.
 */
function visGetTopicSize(topic) {
  if (!visDoVaryCellW()) return 1;
  
  var size = topic[state.vis.topicSize.attr];
  return (size <= CONST.vis.minCellSizeRatio ? CONST.vis.minCellSizeRatio : size);
}


// ------------------------------------------------------------------------------------------------------
function ehVisGridMouseOver(e, g, miniSvg) {
    
    miniSvg.setVis(true, 0, 250);
}


// ------------------------------------------------------------------------------------------------------
function ehVisGridMouseOut(e, g, miniSvg) {
  miniSvg.setVis(false, 0, 250);
}


// ------------------------------------------------------------------------------------------------------
function ehVisGridBoxMouseOver(e, grpOutter, gridData, miniSvg, miniSeries) {
  var grpOutterNode = grpOutter.node();
  var grpInner      = grpOutter.select(".grid-cell-inner");
  var box           = grpInner.select(".box");
  var topicIdx      = +grpOutter.attr("data-topic-idx");
  var cellIdx       = +grpOutter.attr("data-cell-idx");
  var gridName      = grpOutter.attr("data-grid-name");
  
  var cx = box.attr("width")  / 2;
  var cy = box.attr("height") / 2;
  
  /*
  for (var i=0, ni=box.node().parentNode.childNodes.length; i < ni; i++) {
    var child = box.node().parentNode.childNodes[i];
    if (child === box.node()) continue;
    d3.select(child).attr("filter", "url(#blur)");
  }
  */
  
  grpOutterNode.parentNode.appendChild(grpOutterNode);  // make the first element to move to top
  
  if (state.args.uiGridActLstMode) {
    grpInner.
      transition().delay(0).duration(100).ease("easeInOutQuart").
      attrTween("transform", function (d,i,a) {
        if (!visDoVaryCellW()) {
          return d3.interpolateString("rotateX(0," + cx + "," + cy + ")", "rotate(45," + cx + "," + cy + ")");
        }
      });
    
    box.
      transition().delay(0).duration(100).ease("easeInOutQuart").
      attr("rx", 1).  // TODO: Change for 0 in chrome (Safari fucks up corners with 0)
      attr("ry", 1).
      style("stroke", "black").
      attr("filter", "url(#shadow)");
  }
  
  else {
    if (state.vis.grid.cellIdxSel !== cellIdx) {
      grpInner.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attrTween("transform", function (d,i,a) {
          if (!visDoVaryCellW()) {
            return d3.interpolateString("rotateX(0," + cx + "," + cy + ")", "rotate(45," + cx + "," + cy + ")");
          }
        });
      
      box.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attr("rx", 1).  // TODO: Change for 0 in chrome (Safari fucks up corners with 0)
        attr("ry", 1).
        style("stroke", "black").
        attr("filter", "url(#shadow)");
    }
    else {
      grpInner.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attrTween("transform", function (d,i,a) {
          if (!visDoVaryCellW()) {
            return d3.interpolateString("rotateX(0," + cx + "," + cy + ")", "rotate(45," + cx + "," + cy + ")");
          }
        });
        
      box.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attr("filter", "url(#shadow)");
    }
  }
  
  if (miniSvg) {
    miniSvg.
      setTitle(grpOutter.attr("data-var-name")).
      updSeries("pri", gridData, parseInt(grpOutter.attr("data-series-idx"))).
      setSeriesItemClass("pri", "").
      setSeriesItemClass("pri", "l-gray", [+grpOutter.attr("data-idx")]);
  }
  
  // Show the activities list for the cell being moused-over if LMB is held down:
  if (state.args.uiGridActLstMode && state.isMouseBtn1) {
    if ((gridName === "me" || gridName === "me vs grp" || gridName === "grp")) {
      if (topicIdx === state.vis.topicIdx && state.vis.grid.name === gridName) return;  // the already-selected topic has been clicked (and on the same grid at that)
      
      state.vis.grid.cellIdxSel = cellIdx;
      state.vis.grid.cellSel    = grpOutter;
      state.vis.topicIdx        = topicIdx;
      state.vis.grid.name       = gridName;
      
      if (state.vis.topicIdx === 0) return actLstHide();  // the average topic has been clicked or the already-selected topic has been clicked
      
      return actLstShow(gridName === "me", gridName === "me vs grp", gridName === "grp");
    }
  }else{
      //return actLstHide(); 
  }
}


// ------------------------------------------------------------------------------------------------------
function ehVisGridBoxMouseOut(e, grpOutter, miniSvg) {
  var grpOutterNode = grpOutter.node();
  var grpInner      = grpOutter.select(".grid-cell-inner");
  var box           = grpInner.select(".box");
  var cellIdx       = +grpOutter.attr("data-cell-idx");
  var gridName      = grpOutter.attr("data-grid-name");
  
  var cx = box.attr("width")  / 2;
  var cy = box.attr("height") / 2;
  
  /*
  for (var i=0, ni=box.node().parentNode.childNodes.length; i < ni; i++) {
    var child = box.node().parentNode.childNodes[i];
    d3.select(child).attr("filter", "");
  }
  */
  
  // (1) Activities list mode:
  if (state.args.uiGridActLstMode) {
    if ((gridName === "act:me" || gridName === "act:me vs grp" || gridName === "act:grp") && state.vis.grid.cellIdxSel === cellIdx) {  // this if-else is reversed from the one in part (2)
      grpInner.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attrTween("transform", function (d,i,a) {
          if (!visDoVaryCellW()) {
            return d3.interpolateString("rotate(45," + cx + "," + cy + ")", "rotate(0," + cx + "," + cy + ")");
          }
        });
      
      box.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attr("filter", "");
    }
    else {
      grpInner.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attrTween("transform", function (d,i,a) {
          if (!visDoVaryCellW()) {
            return d3.interpolateString("rotate(45," + cx + "," + cy + ")", "rotate(0," + cx + "," + cy + ")");
          }
        });
      
      box.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attr("rx", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
        attr("ry", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
        style("stroke", "").
        attr("filter", "");
    }
  }
  
  // (2) Activities grid mode:
  else {
    if (state.vis.grid.cellIdxSel !== cellIdx) {
      grpInner.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attrTween("transform", function (d,i,a) {
          if (!visDoVaryCellW()) {
            return d3.interpolateString("rotate(45," + cx + "," + cy + ")", "rotate(0," + cx + "," + cy + ")");
          }
        });
      
      box.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attr("rx", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
        attr("ry", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
        style("stroke", "").
        attr("filter", "");
    }
    else {
      grpInner.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attrTween("transform", function (d,i,a) {
          if (!visDoVaryCellW()) {
            return d3.interpolateString("rotate(45," + cx + "," + cy + ")", "rotate(0," + cx + "," + cy + ")");
          }
        });
      
      box.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attr("filter", "");
    }
  }
  
  if (miniSvg) {
    miniSvg.
      //zeroSeries("pri", { sepX: data.sepX, series: miniSeries[grpOutter.attr("data-var-id")] }).
      setSeriesItemClass("pri", "l-gray").
      setVis(false);
  }
}


// ------------------------------------------------------------------------------------------------------
function ehVisGridBoxClick(e, grpOutter) {
  var grpOutterNode = grpOutter.node();
  var grpInner      = grpOutter.select(".grid-cell-inner");
  var box           = grpInner.select(".box");
  var seq           = grpInner.select(".seq");
  var idx           = +grpOutter.attr("data-idx") - 1;
  var topicIdx      = +grpOutter.attr("data-topic-idx");
  var resIdx        = +grpOutter.attr("data-res-idx");
  var actIdx        = +grpOutter.attr("data-act-idx");
  var cellIdx       = +grpOutter.attr("data-cell-idx");
  var gridName      = grpOutter.attr("data-grid-name");
  var row           = grpOutter.attr("data-series-idx");
  var topic         = data.topics[topicIdx];
  var res           = data.resources[resIdx];
  var act           = (actIdx === -1 ? null : topic.activities[res.id][actIdx]);
  
 
  // (1) Activities list mode:
  if (state.args.uiGridActLstMode) {
    // (1.1) Topics grid:
    if ((gridName === "me" || gridName === "me vs grp" || gridName === "grp")) {
      if (topicIdx === state.vis.topicIdx && state.vis.grid.name === gridName) return;  // the already-selected topic has been clicked (and on the same grid at that)
      
      state.vis.grid.cellIdxSel = cellIdx;
      state.vis.grid.cellSel    = grpOutter;
      state.vis.topicIdx        = topicIdx;
      state.vis.grid.name       = gridName;
      
      if (state.vis.topicIdx === 0) return actLstHide();  // the average topic has been clicked or the already-selected topic has been clicked
      log(
              "action"               + CONST.log.sep02 + "grid-topic-cell-select"     + CONST.log.sep01 +
              "cell-topic-id"    + CONST.log.sep02 + getTopic().id       + CONST.log.sep01 +
              "grid-name"    + CONST.log.sep02 + gridName       + CONST.log.sep01 +
              "resource-id" + CONST.log.sep02 + state.vis.act.resId + CONST.log.sep01 +
              "sequencing" + CONST.log.sep02 + grpInner.data()[0].seq,
              true
           );
      //State.vis.resIdx is 0 when OVERALL is selected
      if(state.vis.resIdx >= 0){
          if(row == 0) return actLstShow(true,false,false);
          if(row == 1) return actLstShow(false,true,false);
          if(row == 2) return actLstShow(false,false,true);
      }else{
          if(state.args.uiGridMeGrpVis || state.args.uiGridGrpVis)
              return actLstShow(gridName === "me", gridName === "me vs grp", gridName === "grp");
          else 
              return actLstShow(true, false, false);
      }
      
    }
    
    // (1.2) Activities grid:
    else {
      if (actIdx === -1) return;  // the average activity cell has been clicked
      
      // (1.2.1) Deselect the currently selected cell:
      if (state.vis.grid.cellSel !== null) {
        var boxSel = state.vis.grid.cellSel.select(".grid-cell-inner").select(".box");
        var seqSel = state.vis.grid.cellSel.select(".grid-cell-inner").select(".seq");
        
        boxSel.
          transition().delay(0).duration(100).ease("easeInOutQuart").
          attr("rx", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
          attr("ry", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
          style("stroke-width", "1").
          style("stroke", "");
        
        seqSel.style("fill", CONST.vis.colors.me[6][5]);
      }
      
      // (1.2.2) Select the new cell:
      box.
        transition().delay(0).duration(100).ease("easeInOutQuart").
        attr("rx", (!visDoVaryCellW() ? 20 : 0)).
        attr("ry", (!visDoVaryCellW() ? 20 : 0)).
        style("stroke-width", (!visDoVaryCellW() ? 1.51 : 1.51)).
        style("stroke", "black");
      
      seq.style("fill", "#000000");
      
      state.vis.grid.cellIdxSel = cellIdx;
      state.vis.grid.cellSel    = grpOutter;
      
              
      log(
        "action"           + CONST.log.sep02 + "grid-activity-cell-select" + CONST.log.sep01 +
        "grid-name"        + CONST.log.sep02 + gridName                    + CONST.log.sep01 +
        "cell-topic-id"    + CONST.log.sep02 + topic.id                    + CONST.log.sep01 +
        "cell-resource-id" + CONST.log.sep02 + res.id                      + CONST.log.sep01 +
        "cell-activity-id" + CONST.log.sep02 + act.id                      + CONST.log.sep01 + 
        "sequencing"       + CONST.log.sep02 + grpInner.data()[0].seq,
        true
      );
      
      if (actIdx !== -1) actOpen(res.id, actIdx);
      
      return;
    }
  }
  
  // (2) Activities grid mode:
  else {
    // (2.1) Select:
    if (state.vis.grid.cellIdxSel !== cellIdx) {
      // (2.1.1) Topic grid -- The average topic has been clicked:
      if (getTopic() === null && idx === -1) return;
      
      // (2.1.2) Topic grid -- A topic has been clicked so we switch to activity grid:
      if (getTopic() === null && idx !== -1) {
        state.vis.grid.cellIdxSel = cellIdx;
        state.vis.grid.cellSel    = grpOutter;
        state.vis.topicIdx        = topicIdx;
        state.vis.grid.name       = gridName;
        
        // ui.nav.tabs.tabs.find(".ui-tabs-nav").children(0).children(0)[0].innerHTML = "TOPIC: " + topic.name;
          // [I've since removed the top tabs altogether, but I kept this code in case this comes in handy later]
        
        log(
          "action"           + CONST.log.sep02 + "grid-topic-cell-select" + CONST.log.sep01 +
          "grid-name"        + CONST.log.sep02 + gridName                 + CONST.log.sep01 +
          "cell-topic-id"    + CONST.log.sep02 + topic.id                 + CONST.log.sep01 +
          "cell-resource-id" + CONST.log.sep02 + res.id,
          true
        );
        
        return visDo(true, true, true);
      }
      
      // (2.1.3) Activity grid -- The average activity has been clicked so we go back to the topic grid:
      if (getTopic() !== null && idx === -1) {
        state.vis.grid.cellIdxSel = -1;
        state.vis.grid.cellSel    = null;
        state.vis.topicIdx        = -1;
        state.vis.grid.name       = null;
        
        //ui.nav.tabs.tabs.find(".ui-tabs-nav").children(0).children(0)[0].innerHTML = "TOPICS";
          // TODO: Set page header
        
        log(
          "action"           + CONST.log.sep02 + "grid-activity-go-back" + CONST.log.sep01 +
          "grid-name"        + CONST.log.sep02 + gridName                + CONST.log.sep01 +
          "cell-topic-id"    + CONST.log.sep02 + topic.id                + CONST.log.sep01 +
          "cell-resource-id" + CONST.log.sep02 + res.id,
          true
        );
        
        return visDo(true, true, true);
      }
      
      // (2.1.4) Activity grid -- An activity has been clicked so we mark it as selected and open it:
      if (getTopic() !== null && idx !== -1) {
        // (1.4.1) Deselect the currently selected cell:
        if (state.vis.grid.cellSel !== null) {
          var boxSel = state.vis.grid.cellSel.select(".grid-cell-inner").select(".box");
          var seqSel = state.vis.grid.cellSel.select(".grid-cell-inner").select(".seq");
          
          boxSel.
            transition().delay(0).duration(100).ease("easeInOutQuart").
            attr("rx", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
            attr("ry", (!visDoVaryCellW() ? state.vis.grid.cornerRadius : 0)).
            style("stroke-width", "1").
            style("stroke", "");
          
          seqSel.style("fill", CONST.vis.colors.me[6][5]);
        }
        
        // (2.1.4.2) Select the new cell:
        box.
          transition().delay(0).duration(100).ease("easeInOutQuart").
          attr("rx", (!visDoVaryCellW() ? 20 : 0)).
          attr("ry", (!visDoVaryCellW() ? 20 : 0)).
          style("stroke-width", (!visDoVaryCellW() ? 1.51 : 1.51)).
          style("stroke", "black");
        
        seq.style("fill", "#000000");
        
        state.vis.grid.cellIdxSel = cellIdx;
        state.vis.grid.cellSel    = grpOutter;
        
        log(
          "action"           + CONST.log.sep02 + "grid-activity-cell-select" + CONST.log.sep01 +
          "grid-name"        + CONST.log.sep02 + gridName                    + CONST.log.sep01 +
          "cell-topic-id"    + CONST.log.sep02 + topic.id                    + CONST.log.sep01 +
          "cell-resource-id" + CONST.log.sep02 + res.id                      + CONST.log.sep01 +
          "cell-activity-id" + CONST.log.sep02 + act.id,
          true
        );
        
        if (actIdx !== -1) actOpen(res.id, actIdx);
        
        return;
      }
      
      // Sunburst (randomize to test; old stuff):
      /*
      var r = Math.random();
           if (r < 0.25) visGenSunburst(visGenSunburstData(topic, null, null, ["#eeeeee"].concat(colorbrewer.Blues[6])));
      else if (r < 0.50) visGenSunburst(visGenSunburstData(topic, null, null, ["#eeeeee"].concat(colorbrewer.PuRd[6] )));
      else if (r < 0.75) visGenSunburst(visGenSunburstData(topic, null, null, ["#eeeeee"].concat(colorbrewer.Greys[6])));
      else               visGenSunburst(visGenSunburstData(topic, null, null, colorbrewer.GrpRev[6].concat(["#eeeeee"], colorbrewer.PuRd[6])));
      */
    }
    
    // (2.2) Deselect:
    else {
      grpOutterNode.parentNode.appendChild(grpOutterNode);  // make the first element to move to top
      
      // (2.2.1) Activity grid -- An activity has been clicked so we deselect it:
      if (getTopic() !== null && idx !== -1) {
        box.
          transition().delay(0).duration(100).ease("easeInOutQuart").
          attr("filter", "url(#shadow)").
          attr("rx", 1).  // TODO: Change for 0 in Chrome (Safari fucks up corners with 0)
          attr("ry", 1).
          style("stroke-width", "1").
          style("stroke", "black");
        
        seq.style("fill", CONST.vis.colors.me[6][5]);
      }
      
      state.vis.grid.cellIdxSel = -1;
      state.vis.grid.cellSel    = null;
      
      return;
      
      /*
      $removeChildren(ui.vis.sunburst);
      */
    }
  }
}


// ------------------------------------------------------------------------------------------------------
/**
 * Generates a sunburst visualization.
 * 
 * http://bl.ocks.org/mbostock/4063423
 * http://strongriley.github.io/d3/ex/sunburst.html
 */
function visGenSunburst(sunburstData) {
  var D = {
    name: "A", ratio: 1, val: 4,  // topic
    children: [
      {
        name: "a", ratio: 0.20, val: 2,
        children: [
          { name: "a1", ratio: 0.60, val: 2 },
          { name: "a2", ratio: 0.20, val: 1 },
          { name: "a3", ratio: 0.10, val: 2 },
          { name: "a4", ratio: 0.05, val: 2 },
          { name: "a5", ratio: 0.05, val: 2 }
        ]
      },
      {
        name: "b", ratio: 0.20, val: 4,
        children: [
          { name: "b1", ratio: 0.90, val: 3 },
          { name: "b2", ratio: 0.10, val: 5 }
        ]
      },
      {
        name: "c", ratio: 0.40, val: 6,
        children: [
          { name: "c1", ratio: 0.30, val: 6 },
          { name: "c2", ratio: 0.30, val: 5 },
          { name: "c3", ratio: 0.40, val: 5 }
        ]
      },
      {
        name: "d", ratio: 0.10, val: 6,
        children: [
          { name: "d1", ratio: 0.30, val: 6 },
          { name: "d2", ratio: 0.30, val: 6 },
          { name: "d3", ratio: 0.40, val: 5 }
        ]
      },
      {
        name: "e", ratio: 0.10, val: 6,
        children: [
          { name: "e1", ratio: 0.30, val: 6 },
          { name: "e2", ratio: 0.30, val: 6 },
          { name: "e3", ratio: 0.40, val: 6 }
        ]
      }
    ]
  };
  
  var w = 300;
  var h = 300;
  var r = Math.min(w,h) / 2;
  var color = d3.scale.category20c();
  
  $removeChildren(ui.vis.sunburst);
  
  var svg = d3.
    select(ui.vis.sunburst).
    append("svg").
    attr("width", w).
    attr("height", h).
    append("g").
    attr("transform", "translate(" + (w / 2) + "," + (h / 2) + ")");
  
  var partition = d3.layout.partition().
    sort(null).
    size([2 * Math.PI, r * r]).
    //value(function (d) { return 1; });
    value(function (d) { return d.ratio; });
  
  var arc = d3.svg.arc().
    startAngle(function (d) { return d.x; }).
    endAngle(function (d) { return d.x + d.dx; }).
    innerRadius(function (d) { return Math.sqrt(d.y); }).
    outerRadius(function (d) { return Math.sqrt(d.y + d.dy); });
  
  var path = svg.
    datum(D).
    selectAll("path").
    data(partition.nodes).
      enter().
      append("path").
      attr("display", function (d) { return d.depth ? null : "none"; }).  // hide the most inner ring
      attr("d", arc).
      style("stroke", "#ffffff").
      style("fill", function (d) { return sunburstData.colorScale[d.val]; }).
      style("fill-rule", "evenodd").
      style("shape-rendering", "geometricPrecision").
      each(stash);
  
  /*
  path.
    data(partition.value(function (d) { return d.ratio; }).nodes).
    transition().
    duration(1500).
    attrTween("d", arcTween);
  */
  
  /*
  d3.selectAll("input").on("change", function change() {
    var value = this.value === "count"
      ? function() { return 1; }
      : function(d) { return d.size; };
    
    path.
      data(partition.value(value).nodes).
      transition().
      duration(1500).
      attrTween("d", arcTween);
  });
  */
  
  // Stash the old values for transition.
  function stash(d) {
    d.x0  = d.x;
    d.dx0 = d.dx;
  }
  
  // Interpolate the arcs in data space.
  function arcTween(a) {
    var i = d3.interpolate({ x: a.x0, dx: a.dx0 }, a);
    return function(t) {
      var b = i(t);
      a.x0 = b.x;
      a.dx0 = b.dx;
      return arc(b);
    };
  }
  
  //d3.select(self.frameElement).style("height", h + "px");
}


// ------------------------------------------------------------------------------------------------------
function visResetAll() {
  // Remove all existing tables which hold visualizations:
  for (var i = 0; i < ui.vis.tbl.length; i++) {
    if (ui.vis.tbl[i]) ui.vis.tbl[i].parentNode.parentNode.removeChild(ui.vis.tbl[i].parentNode);
  }
  ui.vis.tbl = [];
  
  // Activities (old version with the activity loaded into a new tab):
  /*
  $removeChildren(ui.vis.act);
  $hide(ui.vis.act);
  */
  
  // Other:
  state.vis.grid.cellIdxMax = 0;
  state.vis.grid.cellIdxSel = -1;
  state.vis.grid.cellSel    = null;
}


// ------------------------------------------------------------------------------------------------------
function visToggleSeries(name) {
  var svg01 = ui.vis.series[name][0];
  var svg02 = ui.vis.series[name][1];
  
  if (svg01.style.display === "block") {
    $hide(svg01);
    $hide(svg02);
  }
  else {
    $show(svg01);
    $show(svg02);
  }
}

//------------------------------------------------------------------------------------------------------
function CalculateStarPoints(centerX, centerY, arms, outerRadius, innerRadius){
   var results = "";
 
   var angle = Math.PI / arms;
   for (var i = 0; i < 2 * arms; i++){
      // Use outer or inner radius depending on what iteration we are in.
      var r = (i & 1) == 0 ? outerRadius : innerRadius;
      
      var currX = centerX + Math.cos(i * angle) * r;
      var currY = centerY + Math.sin(i * angle) * r;
 
      // Our first time we simply append the coordinates, subsequet times
      // we append a ", " to distinguish each coordinate pair.
      if (i == 0){
         results = currX + "," + currY;
      }
      else{
         results += ", " + currX + "," + currY;
      }
   }
   return results;
}


function generateHelp(origin){
    var helpText = "";
    if(origin === "one-res-me-h"){
        helpText = "<h3 style='margin: 0px; padding: 0px 10px 0px 0px;'>My Progress Grid</h3><p>This row represents your progress in the topics of the course. Each topic is a cell. Gray means 0% of progress and darker color means more progress.</p>";
        helpText += "<table border=0 cellpadding=0 cellspacing=0>";
        helpText += "<tr>" +
        		"<td style='padding:2px 5px 2px 0px;'>0%</td>" +
        		"<td style='background-color:rgb(238, 238, 238); padding:2px 5px 2px 5px;'>&nbsp;</td>" +
        		"<td style='background-color:#edf8e9; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
        		"<td style='background-color:#c7e9c0; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
        		"<td style='background-color:#a1d99b; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
        		"<td style='background-color:#74c476; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
        		"<td style='background-color:#31a354; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
        		"<td style='background-color:#006d2c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
        		"<td style='padding:2px 0px 2px 5px;'>100%</td>" +
        		"</tr>";
        helpText += "</table>";
        //"#edf8e9","#c7e9c0","#a1d99b","#74c476","#31a354","#006d2c"
    }
    if(origin === "one-res-mevsgrp-h"){
        helpText = "<h3 style='margin: 0px; padding: 0px 10px 0px 0px;'>Comparison Grid</h3><p style='margin-top: 2px;margin-bottom:5px;'>This row shows the <i>difference</i> between your progress and the average progress of other students. <span style='color: #006d2c;font-weight:bold;'>GREEN</span> color means you have more progress than the others and <span style='color: #08519c;font-weight:bold;'>BLUE</span> color means that in average other students are more advance than you. Gray means equal progress. </p>";
        helpText += "<table border=0 cellpadding=0 cellspacing=0>";
        helpText += "<tr>" +
                "<td style='padding:2px 5px 2px 0px;font-size: 10px;'>group +</td>" +
                "<td style='background-color:#08519c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#3182bd; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#6baed6; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#9ecae1; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#c6dbef; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:rgb(238, 238, 238); padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#c7e9c0; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#a1d99b; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#74c476; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#31a354; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#006d2c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='padding:2px 0px 2px 5px;font-size: 10px;'>you +</td>" +
                "</tr>";
        helpText += "</table>";
    }
    if(origin === "one-res-grp-h"){
        helpText = "<h3 style='margin: 0px; padding: 0px 10px 0px 0px;'>Group Grid</h3><p>This row shows the average of progress of other students in the class using <span style='color: #08519c;font-weight:bold;'>BLUE</span> colors. Depending on the set up of Mastery Grids, others students might include all the class or top students.</p>";
        helpText += "<table border=0 cellpadding=0 cellspacing=0>";
        helpText += "<tr>" +
                "<td style='padding:2px 5px 2px 0px;'>0%</td>" +
                "<td style='background-color:rgb(238, 238, 238); padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#eff3ff; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#c6dbef; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#9ecae1; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#6baed6; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#3182bd; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#08519c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='padding:2px 0px 2px 5px;'>100%</td>" +
                "</tr>";
        helpText += "</table>";
        //["#eff3ff","#c6dbef","#9ecae1","#6baed6","#3182bd","#08519c"],
    }
    if(origin === "all-res-me-h"){
        helpText = "<h3 style='margin: 0px; padding: 0px 10px 0px 0px;'>My Progress Grid</h3><p style='margin-top: 2px;'>This grid represents your progress in the topics. Each topic is a column. " +
        		   "First row shows <b>average</b> across different types of content. Other rows shows progress within specific types of content (quizzes, examples). Gray means 0% of progress and darker color means more progress.</p>";
        helpText += "<table border=0 cellpadding=0 cellspacing=0>";
        helpText += "<tr>" +
                "<td style='padding:2px 5px 2px 0px;'>0%</td>" +
                "<td style='background-color:rgb(238, 238, 238); padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#edf8e9; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#c7e9c0; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#a1d99b; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#74c476; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#31a354; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#006d2c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='padding:2px 0px 2px 5px;'>100%</td>" +
                "</tr>";
        helpText += "</table>";
    }
    if(origin === "all-res-mevsgrp-h"){
        helpText = "<h3 style='margin: 0px; padding: 0px 10px 0px 0px;'>Comparison Grid</h3><p style='margin-top: 2px;margin-bottom:5px;'>" +
        		"This grid shows the <i>difference</i> between your progress (<span style='color: #006d2c;font-weight:bold;'>GREEN</span>) and other students progress (<span style='color: #08519c;font-weight:bold;'>BLUE</span>). The cell are colored depending on this difference: if you see a green cell, it means you are more advance than the average of other students in the corresponding topic.</p>";        
        helpText += "<table border=0 cellpadding=0 cellspacing=0>";
        helpText += "<tr>" +
                "<td style='padding:2px 5px 2px 0px;font-size: 10px;'>group +</td>" +
                "<td style='background-color:#08519c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#3182bd; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#6baed6; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#9ecae1; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#c6dbef; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:rgb(238, 238, 238); padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#c7e9c0; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#a1d99b; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#74c476; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#31a354; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#006d2c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='padding:2px 0px 2px 5px;font-size: 10px;'>you +</td>" +
                "</tr>";
        helpText += "</table>";
    }
    if(origin === "all-res-grp-h"){
        helpText = "<h3 style='margin: 0px; padding: 0px 10px 0px 0px;'>Group progress</h3><p>This grid shows the average of progress of other students in the class using . Depending on the set up of Mastery Grids, others students might include all the class or top students using <span style='color: #08519c;font-weight:bold;'>BLUE</span> colors.</p>";        
        helpText += "<table border=0 cellpadding=0 cellspacing=0>";
        helpText += "<tr>" +
                "<td style='padding:2px 5px 2px 0px;'>0%</td>" +
                "<td style='background-color:rgb(238, 238, 238); padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#eff3ff; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#c6dbef; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#9ecae1; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#6baed6; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#3182bd; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='background-color:#08519c; padding:2px 5px 2px 5px;'>&nbsp;</td>" +
                "<td style='padding:2px 0px 2px 5px;'>100%</td>" +
                "</tr>";
        helpText += "</table>";
    }
    return helpText;
}