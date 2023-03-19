"use strict";!function(t,e){"object"==typeof exports&&"object"==typeof module?module.exports=e():"function"==typeof define&&define.amd?define("itowns_widgets",[],e):"object"==typeof exports?exports.itowns_widgets=e():t.itowns_widgets=e()}(self,(()=>(self.webpackChunkitowns=self.webpackChunkitowns||[]).push([[318],{75462:(t,e,o)=>{o.r(e),o.d(e,{Minimap:()=>D,Navigation:()=>L,Scale:()=>O,Searchbar:()=>j});var n=o(15671),i=o(43144),a=o(97326),r=o(79340),c=o(82963),s=o(61120),l=o(13092),d=o(86033),u=o(41933);var m=new WeakMap;const p=function(){function t(e){var o,i,a,r=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},c=arguments.length>2?arguments[2]:void 0;(0,n.Z)(this,t),a={writable:!0,value:void 0},function(t,e){if(e.has(t))throw new TypeError("Cannot initialize the same private elements twice on an object")}(o=this,i=m),i.set(o,a),this.parentElement=r.parentElement||e.domElement,this.position=r.position||c.position,["top-left","top-right","bottom-left","bottom-right","top","bottom","left","right"].includes(this.position)||(console.warn("'position' optional parameter for 'Widget' constructor is not a valid option. "+"It will be set to '".concat(c.position,"'.")),this.position=c.position),this.domElement=document.createElement("div"),this.parentElement.appendChild(this.domElement),this.domElement.style.width="".concat(r.width||r.size||c.width,"px"),this.domElement.style.height="".concat(r.height||r.size||c.height,"px");var s=this.position.split("-");if(this.domElement.classList.add("".concat(s[0],"-widget")),s[1])this.domElement.classList.add("".concat(s[1],"-widget"));else switch(s[0]){case"top":case"bottom":this.domElement.style.left="calc(50% - ".concat(this.domElement.offsetWidth/2,"px)");break;case"left":case"right":this.domElement.style.top="calc(50% - ".concat(this.domElement.offsetHeight/2,"px)")}r.translate&&(this.domElement.style.transform="translate(".concat(r.translate.x||0,"px, ").concat(r.translate.y||0,"px)")),this.domElement.addEventListener("pointerdown",(function(t){t.stopPropagation()})),this.domElement.addEventListener("mousedown",(function(t){t.stopPropagation()}))}return(0,i.Z)(t,[{key:"show",value:function(){this.domElement.style.display=(0,d.Z)(this,m)}},{key:"hide",value:function(){(0,l.Z)(this,m,window.getComputedStyle(this.domElement).display),this.domElement.style.display="none"}}]),t}();function f(t,e){h(t,e),e.add(t)}function h(t,e){if(e.has(t))throw new TypeError("Cannot initialize the same private elements twice on an object")}function v(t,e,o){if(!e.has(t))throw new TypeError("attempted to get private field on non-instance");return o}var w={displayCompass:!0,display3DToggle:!0,displayZoomIn:!0,displayZoomOut:!0,animationDuration:500,position:"bottom-left",direction:"column"},g={compass:{id:"compass",content:"",info:"Rotate the camera to face North",parentId:"widgets"},toggle3D:{id:"3d-button",content:"3D",info:"Tilt the camera"},zoomIn:{id:"zoom-in-button",content:'<span class="widget-zoom-button-logo"></span>',info:"Zoom in",parentId:"zoom-button-bar"},zoomOut:{id:"zoom-out-button",content:'<span id="zoom-out-logo" class="widget-zoom-button-logo"></span>',info:"Zoom out",parentId:"zoom-button-bar"}},E=new WeakMap,y=new WeakSet,Z=new WeakSet;function b(t){return t.time=this.animationDuration,(0,d.Z)(this,E).controls.lookAtCoordinate(t)}function x(t,e){return this.addButton(t.id,t.content,t.info,e,t.parentId)}const L=function(t){(0,r.Z)(p,t);var e,o,m=(e=p,o=function(){if("undefined"==typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"==typeof Proxy)return!0;try{return Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],(function(){}))),!0}catch(t){return!1}}(),function(){var t,n=(0,s.Z)(e);if(o){var i=(0,s.Z)(this).constructor;t=Reflect.construct(n,arguments,i)}else t=n.apply(this,arguments);return(0,c.Z)(this,t)});function p(t){var e,o,i,r,c,s,L,z,S=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{};if((0,n.Z)(this,p),!t.isGlobeView)throw new Error("'Navigation' plugin only supports 'GlobeView'. Therefore, the 'view' parameter must be a 'GlobeView'.");return["top","bottom","left","right"].includes(S.position)&&(console.warn("'position' optional parameter for 'Navigation' is not a valid option. "+"It will be set to '".concat(w.position,"'.")),S.position=w.position),c=m.call(this,t,S,w),f((0,a.Z)(c),Z),f((0,a.Z)(c),y),z={writable:!0,value:void 0},h(s=(0,a.Z)(c),L=E),L.set(s,z),(0,l.Z)((0,a.Z)(c),E,t),c.direction=S.direction||w.direction,["column","row"].includes(c.direction)||(console.warn("'direction' optional parameter for 'Navigation' constructor is not a valid option. "+"It will be set to '".concat(w.direction,"'.")),c.direction=w.direction),c.animationDuration=void 0===S.animationDuration?w.animationDuration:S.animationDuration,c.domElement.id="widgets-navigation",c.domElement.classList.add("".concat(c.direction,"-widget")),(null!==(e=S.displayCompass)&&void 0!==e?e:w.displayCompass)&&(c.compass=v((0,a.Z)(c),Z,x).call((0,a.Z)(c),g.compass,(function(){v((0,a.Z)(c),y,b).call((0,a.Z)(c),{heading:0,tilt:89.5})})),t.addEventListener(u.b.CAMERA_MOVED,(function(t){c.compass.style.transform="rotate(".concat(-t.heading,"deg)")}))),(null!==(o=S.display3DToggle)&&void 0!==o?o:w.display3DToggle)&&(c.toggle3D=v((0,a.Z)(c),Z,x).call((0,a.Z)(c),g.toggle3D,(function(){v((0,a.Z)(c),y,b).call((0,a.Z)(c),{tilt:(0,d.Z)((0,a.Z)(c),E).controls.getTilt()<89?89.5:40})})),t.addEventListener(u.b.CAMERA_MOVED,(function(t){c.toggle3D.innerHTML=t.tilt<89?"2D":"3D"}))),(null!==(i=S.displayZoomIn)&&void 0!==i?i:w.displayZoomIn)&&(c.zoomIn=v((0,a.Z)(c),Z,x).call((0,a.Z)(c),g.zoomIn,(function(){v((0,a.Z)(c),y,b).call((0,a.Z)(c),{zoom:Math.min(20,(0,d.Z)((0,a.Z)(c),E).controls.getZoom()+1)})}))),(null!==(r=S.displayZoomOut)&&void 0!==r?r:w.displayZoomOut)&&(c.zoomOut=v((0,a.Z)(c),Z,x).call((0,a.Z)(c),g.zoomOut,(function(){v((0,a.Z)(c),y,b).call((0,a.Z)(c),{zoom:Math.max(3,(0,d.Z)((0,a.Z)(c),E).controls.getZoom()-1)})}))),c}return(0,i.Z)(p,[{key:"addButton",value:function(t){var e=this,o=arguments.length>1&&void 0!==arguments[1]?arguments[1]:"",n=arguments.length>2&&void 0!==arguments[2]?arguments[2]:"",i=arguments.length>3&&void 0!==arguments[3]?arguments[3]:function(){},a=arguments.length>4?arguments[4]:void 0,r=document.getElementById(a);r||(r=this.addButtonBar(a));var c=document.createElement("button");return c.className="widget-button",c.id=t,c.innerHTML=o,c.title=n,c.onclick=i,r.appendChild(c),c.tabIndex=-1,window.addEventListener("pointerup",(function(){document.activeElement===c&&(0,d.Z)(e,E).domElement.focus()})),c}},{key:"addButtonBar",value:function(t){var e=document.createElement("div");return e.className="widget-button-bar",t&&(e.id=t),this.domElement.appendChild(e),e}}]),p}(p);var z=o(60145),S=o(49469),C=o(48682),R=o(800);var M={minScale:2e-6,maxScale:2e-9,zoomRatio:1/30,width:150,height:150,position:"bottom-left"};const D=function(t){(0,r.Z)(l,t);var e,o,a=(e=l,o=function(){if("undefined"==typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"==typeof Proxy)return!0;try{return Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],(function(){}))),!0}catch(t){return!1}}(),function(){var t,n=(0,s.Z)(e);if(o){var i=(0,s.Z)(this).constructor;t=Reflect.construct(n,arguments,i)}else t=n.apply(this,arguments);return(0,c.Z)(this,t)});function l(t,e){var o,i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{};if((0,n.Z)(this,l),!t.isGlobeView)throw new Error("'Minimap' plugin only supports 'GlobeView'. Therefore, the 'view' parameter must be a 'GlobeView'.");if(!e.isColorLayer)throw new Error("'layer' parameter form 'Minimap' constructor should be a 'ColorLayer'.");if((o=a.call(this,t,i,M)).minScale=i.minScale||M.minScale,o.maxScale=i.maxScale||M.maxScale,o.zoomRatio=i.zoomRatio||M.zoomRatio,o.domElement.id="widgets-minimap",i.cursor){var r=document.createElement("div");r.id="cursor-wrapper",o.domElement.appendChild(r),"string"==typeof i.cursor?r.innerHTML=i.cursor:i.cursor instanceof HTMLElement&&r.appendChild(i.cursor)}o.view=new C.Z(o.domElement,e.source.extent,{camera:{type:R.P.ORTHOGRAPHIC},placement:e.source.extent,noControls:!0,maxSubdivisionLevel:t.tileLayer.maxSubdivisionLevel,disableFocusOnStart:!0}),o.view.addLayer(e),o.domElement.addEventListener("pointerdown",(function(t){t.preventDefault()}));var c=o.view.camera.camera3D,s=o.view.getScale(i.pitch),d=c.zoom*o.maxScale/s,u=c.zoom*o.minScale/s,m=new z.Z(t.referenceCrs),p=new z.Z(o.view.referenceCrs),f=t.controls.getCameraTargetPosition();return t.addFrameRequester(S.Ao.AFTER_RENDER,(function(){var e=t.camera.camera3D.position.distanceTo(f),n=t.getScaleFromDistance(i.pitch,e);c.zoom=o.zoomRatio*u*n/o.minScale,c.zoom=Math.min(Math.max(c.zoom,d),u),c.updateProjectionMatrix(),m.setFromVector3(t.controls.getCameraTargetPosition()),m.as(o.view.referenceCrs,p),c.position.x=p.x,c.position.y=p.y,c.updateMatrixWorld(!0),o.view.notifyChange(c)})),o}return(0,i.Z)(l)}(p);var T=o(11925),k=o(91742),I=o(44450);var A={width:200,height:30,position:"bottom-left"};const O=function(t){(0,r.Z)(l,t);var e,o,a=(e=l,o=function(){if("undefined"==typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"==typeof Proxy)return!0;try{return Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],(function(){}))),!0}catch(t){return!1}}(),function(){var t,n=(0,s.Z)(e);if(o){var i=(0,s.Z)(this).constructor;t=Reflect.construct(n,arguments,i)}else t=n.apply(this,arguments);return(0,c.Z)(this,t)});function l(t){var e,o=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{};return(0,n.Z)(this,l),(e=a.call(this,t,o,A)).domElement.id="widgets-scale",e.view=t,e.domElement.innerHTML="Scale",e.width=o.width||A.width,e.view.isGlobeView?(e.view.addEventListener(k.b.GLOBE_INITIALIZED,(function(){e.update()})),e.view.controls.addEventListener(T.Q.RANGE_CHANGED,(function(){e.update()}))):e.view.isPlanarView?(e.view.addEventListener(u.b.INITIALIZED,(function(){e.update()})),e.view.addEventListener(I.uZ.MOVED,(function(){e.update()}))):console.warn("The 'view' linked to scale widget is neither a 'GlobeView' nor a 'PlanarView'. The scale wont automatically update. You can implement its update automation using 'Scale.update' method."),e}return(0,i.Z)(l,[{key:"addEventListeners",value:function(){}},{key:"update",value:function(){var t=Math.round(this.view.getPixelsToMeters(this.width)),e=Math.pow(10,t.toString().length-1);t=Math.round(t/e)*e;var o=this.view.getMetersToPixels(t),n="m";t>=1e3&&(t/=1e3,n="km"),this.domElement.innerHTML="".concat(t," ").concat(n),this.domElement.style.width="".concat(o,"px")}}]),l}(p);var P=o(33347);var B={width:300,height:38,position:"top",maxSuggestionNumber:10,fontSize:16,placeholder:"Search location"};function N(t,e){var o;return t?(V(t),e>=t.length?e=0:e<0&&(e=t.length-1),null===(o=t[e])||void 0===o||o.classList.add("active"),e):e}function V(t){for(var e=0;e<t.length;e++)t[e].classList.remove("active")}function G(t){for(;t.children.length>1;)t.removeChild(t.lastChild)}var H=new WeakMap;const j=function(t){(0,r.Z)(m,t);var e,o,u=(e=m,o=function(){if("undefined"==typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"==typeof Proxy)return!0;try{return Boolean.prototype.valueOf.call(Reflect.construct(Boolean,[],(function(){}))),!0}catch(t){return!1}}(),function(){var t,n=(0,s.Z)(e);if(o){var i=(0,s.Z)(this).constructor;t=Reflect.construct(n,arguments,i)}else t=n.apply(this,arguments);return(0,c.Z)(this,t)});function m(t,e){var o,i,r,c,s,p=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{};if((0,n.Z)(this,m),i=u.call(this,t,p,B),r=(0,a.Z)(i),s={writable:!0,value:void 0},function(t,e){if(e.has(t))throw new TypeError("Cannot initialize the same private elements twice on an object")}(r,c=H),c.set(r,s),!e||!e.url||!e.parser||"function"!=typeof e.parser)throw new Error("'geocodingOptions' parameter for 'Searchbar' constructor is not a valid option. Please refer to the documentation.");(0,l.Z)((0,a.Z)(i),H,null!==(o=e.onSelected)&&void 0!==o?o:function(){}),i.domElement.id="widgets-searchbar",i.domElement.style.height="auto";var f=document.createElement("form");f.setAttribute("autocomplete","off"),f.id="searchbar-autocompletion-form",i.domElement.appendChild(f);var h,v=document.createElement("input");v.setAttribute("type","text"),v.setAttribute("name","mySearch"),v.setAttribute("placeholder",p.placeholder||B.placeholder),v.style.height="".concat(p.height||p.size||B.height,"px"),v.style.fontSize="".concat(p.fontSize||B.fontSize,"px"),f.appendChild(v),v.addEventListener("input",(function(){var t=v.value;if(G(f),h=-1,!t)return!1;e.url.searchParams.set("text",t),P.Z.json(e.url).then((function(o){var n=e.parser(o),r=0;n.forEach((function(e,o){if(r!==Math.min(n.size,p.maxSuggestionNumber||B.maxSuggestionNumber)){var c=r;r++;var s=o.toUpperCase().indexOf(t.toUpperCase());if(s>-1){var l=document.createElement("div");l.style.minHeight=v.style.height,l.style.fontSize="".concat(p.fontSize||B.fontSize,"px");var u=o.slice(0,s),m=o.slice(s,s+t.length),w=o.slice(s+t.length,o.length);l.innerHTML="<p>".concat(u,"<strong>").concat(m,"</strong>").concat(w,"</p>"),l.setAttribute("location",o),f.appendChild(l),l.addEventListener("mouseover",(function(){V(f.children),h=c,l.classList.add("active")})),l.addEventListener("click",(function(){(0,d.Z)((0,a.Z)(i),H).call((0,a.Z)(i),e),v.value=l.getAttribute("location"),G(f)}))}}}))}))}));var w=(p.position||B.position).includes("top")?1:-1;return v.addEventListener("keydown",(function(e){e.stopPropagation();var o=f.getElementsByTagName("div");switch(e.code){case"Escape":G(f),v.value="",t.domElement.focus();break;case"ArrowDown":e.preventDefault(),h=N(o,h+w);break;case"ArrowUp":e.preventDefault(),h=N(o,h-w);break;case"Enter":e.preventDefault(),o[Math.max(h,0)]&&(o[Math.max(h,0)].click(),t.domElement.focus())}})),v.addEventListener("focus",(function(){f.classList.add("focus")})),v.addEventListener("blur",(function(){f.classList.remove("focus"),V(f.children)})),f.addEventListener("mouseleave",(function(){V(f.children),h=-1})),i}return(0,i.Z)(m)}(p)}},t=>(75462,t(t.s=75462))])));
//# sourceMappingURL=itowns_widgets.js.map